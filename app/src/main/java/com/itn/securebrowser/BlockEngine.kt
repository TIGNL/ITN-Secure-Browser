package com.itn.securebrowser

import java.util.Calendar

// ══════════════════════════════════════════════════════════════════════════════
// BlockReason — سبب الحجب (يُمرَّر إلى صفحة الحجب لعرض رسالة مناسبة)
// ══════════════════════════════════════════════════════════════════════════════

sealed class BlockReason {
    /** الوقت الحالي داخل نافذة حظر مجدولة. */
    object ScheduleBlock : BlockReason()

    /** استُهلك الحد اليومي المسموح به. */
    data class LimitReached(
        val limitMinutes: Int,
        val usedMinutes: Int
    ) : BlockReason()
}

// ══════════════════════════════════════════════════════════════════════════════
// BlockEngine — يجيب على سؤال واحد: "هل يجب حجب هذا النطاق الآن؟"
// الترتيب: جدول الحظر أولاً ← ثم الحد اليومي
// ══════════════════════════════════════════════════════════════════════════════

class BlockEngine(
    private val dataStore: BlockDataStore,
    private val timeTracker: TimeTracker
) {

    /**
     * يفحص النطاق ويُرجع سبب الحجب، أو null إذا كان الوصول مسموحاً.
     *
     * استخدام:
     *   val reason = blockEngine.check("youtube.com")
     *   if (reason != null) { // احجب وأظهر صفحة الحجب }
     */
    fun check(domain: String): BlockReason? {
        val match = dataStore.findDomain(domain) ?: return null   // النطاق حر

        val (dailyLimits, schedules) = when (match) {
            is BlockMatch.GroupMatch -> match.group.dailyLimits to match.group.schedules
            is BlockMatch.SiteMatch  -> match.site.dailyLimits  to match.site.schedules
        }

        // ① جدول الحظر — يتجاوز الحد اليومي
        if (isInBlockSchedule(schedules)) return BlockReason.ScheduleBlock

        // ② الحد اليومي
        val limitMins = dailyLimits[todayDayName()] ?: return null   // لا حد اليوم
        val usedMins  = (timeTracker.getTodaySeconds(domain) / 60L).toInt()
        if (usedMins >= limitMins) return BlockReason.LimitReached(limitMins, usedMins)

        return null   // مسموح
    }

    // ── فحص الجداول ───────────────────────────────────────────────────────────

    private fun isInBlockSchedule(schedules: List<BlockSchedule>): Boolean {
        val cal       = Calendar.getInstance()
        val today     = dayName(cal.get(Calendar.DAY_OF_WEEK))
        val nowMins   = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        // نحتاج أمس لجداول تعبر منتصف الليل (مثل 21:00 → 07:00)
        val yCal      = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = dayName(yCal.get(Calendar.DAY_OF_WEEK))

        return schedules.any { s ->
            val from = parseTime(s.from)
            val to   = parseTime(s.to)

            if (from <= to) {
                // نافذة عادية لا تعبر منتصف الليل: مثل 08:00 → 14:00
                today in s.days && nowMins in from..to
            } else {
                // نافذة تعبر منتصف الليل: مثل 21:00 → 07:00
                // الجزء المسائي: اليوم في الأيام والوقت بعد البداية
                // الجزء الصباحي: أمس في الأيام والوقت قبل النهاية
                (today in s.days && nowMins >= from) ||
                (yesterday in s.days && nowMins <= to)
            }
        }
    }

    // ── مساعدات ───────────────────────────────────────────────────────────────

    /** يحوّل "HH:mm" إلى دقائق منذ منتصف الليل. */
    private fun parseTime(hhmm: String): Int {
        val (h, m) = hhmm.split(":").map { it.toInt() }
        return h * 60 + m
    }

    private fun todayDayName(): String =
        dayName(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))

    private fun dayName(calDay: Int): String = when (calDay) {
        Calendar.SATURDAY  -> "SATURDAY"
        Calendar.SUNDAY    -> "SUNDAY"
        Calendar.MONDAY    -> "MONDAY"
        Calendar.TUESDAY   -> "TUESDAY"
        Calendar.WEDNESDAY -> "WEDNESDAY"
        Calendar.THURSDAY  -> "THURSDAY"
        Calendar.FRIDAY    -> "FRIDAY"
        else               -> "MONDAY"
    }
}
