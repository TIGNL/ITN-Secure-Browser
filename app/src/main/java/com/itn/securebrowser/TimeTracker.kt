package com.itn.securebrowser

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeTracker(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("itn_time_tracker", Context.MODE_PRIVATE)

    private var trackedDomain: String? = null
    private var sessionStart: Long = 0L

    // ── Public API ─────────────────────────────────────────────────────────

    /** استدعه عند تغيّر النطاق النشط: صفحة جديدة، تبديل تبويب، أو لا شيء. */
    fun onDomainChanged(newDomain: String?) {
        commitCurrentSession()
        trackedDomain = newDomain
        sessionStart = if (newDomain != null) now() else 0L
    }

    /** استدعه في onPause — يحفظ الجلسة الحالية. */
    fun onAppPaused() {
        commitCurrentSession()
    }

    /** استدعه في onResume مع النطاق المرئي حالياً. */
    fun onAppResumed(domain: String?) {
        trackedDomain = domain
        sessionStart = if (domain != null) now() else 0L
    }

    /**
     * إجمالي الثواني المسجّلة اليوم لـ [domain]،
     * يشمل الجلسة الجارية حالياً إن وُجدت.
     */
    fun getTodaySeconds(domain: String): Long {
        val stored = prefs.getLong(key(domain), 0L)
        val live = if (domain == trackedDomain && sessionStart > 0L)
            now() - sessionStart else 0L
        return stored + live
    }

    /** جميع النطاقات التي تم تتبّعها اليوم مع إجمالي ثوانيها (المحفوظة فقط). */
    fun getAllTodayData(): Map<String, Long> {
        val suffix = "_${todayString()}"
        return prefs.all
            .filter  { (k, _) -> k.endsWith(suffix) }
            .mapKeys { (k, _) -> k.removeSuffix(suffix) }
            .mapValues { (_, v) -> (v as? Long) ?: 0L }
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private fun commitCurrentSession() {
        val domain = trackedDomain ?: return
        if (sessionStart <= 0L) return
        val elapsed = now() - sessionStart
        if (elapsed <= 0L) return
        val k = key(domain)
        prefs.edit().putLong(k, prefs.getLong(k, 0L) + elapsed).apply()
        sessionStart = 0L
        trackedDomain = null
    }

    private fun key(domain: String) = "${domain}_${todayString()}"

    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun now() = System.currentTimeMillis() / 1000L

    companion object {
        /** يستخرج النطاق (بدون www.) من URL، أو null إن لم يكن قابلاً للتتبع. */
        fun extractDomain(url: String?): String? {
            if (url.isNullOrBlank() || url == "about:blank") return null
            return try {
                val host = java.net.URI(url).host ?: return null
                host.removePrefix("www.").takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
    }
}
