package com.itn.securebrowser

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

// ══════════════════════════════════════════════════════════════════════════════
// Data classes
// ══════════════════════════════════════════════════════════════════════════════

/**
 * نافذة حظر زمنية.
 * @param days  أسماء الأيام بالإنجليزية الكبيرة: "SATURDAY" … "FRIDAY"
 * @param from  وقت بداية الحظر بصيغة "HH:mm"
 * @param to    وقت نهاية الحظر بصيغة "HH:mm"  (يمكن أن يعبر منتصف الليل)
 */
data class BlockSchedule(
    val days: List<String>,
    val from: String,
    val to: String
)

/**
 * مجموعة مواقع بحدود ورا جداول مشتركة.
 * @param name        اسم المجموعة (مفتاح فريد)
 * @param domains     قائمة النطاقات (بدون www.)
 * @param dailyLimits يوم → دقائق مسموح بها. الأيام الغائبة = بلا حد.
 * @param schedules   نوافذ الحظر الزمنية
 */
data class BlockGroup(
    val name: String,
    val domains: List<String>,
    val dailyLimits: Map<String, Int>,
    val schedules: List<BlockSchedule>
)

/**
 * موقع منفرد خارج أي مجموعة.
 * @param domain      النطاق (بدون www.) — مفتاح فريد
 * @param dailyLimits يوم → دقائق مسموح بها. الأيام الغائبة = بلا حد.
 * @param schedules   نوافذ الحظر الزمنية
 */
data class BlockSite(
    val domain: String,
    val dailyLimits: Map<String, Int>,
    val schedules: List<BlockSchedule>
)

/**
 * نتيجة البحث عن نطاق:
 * - [GroupMatch] إذا كان النطاق ضمن مجموعة
 * - [SiteMatch]  إذا كان موقعاً منفرداً
 */
sealed class BlockMatch {
    data class GroupMatch(val group: BlockGroup) : BlockMatch()
    data class SiteMatch(val site: BlockSite) : BlockMatch()
}

// ══════════════════════════════════════════════════════════════════════════════
// BlockDataStore
// ══════════════════════════════════════════════════════════════════════════════

class BlockDataStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("itn_block_data", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GROUPS = "block_groups"
        private const val KEY_SITES  = "block_sites"
    }

    // ── Groups ────────────────────────────────────────────────────────────────

    /**
     * يحفظ مجموعة (إنشاء أو تعديل حسب الاسم).
     * @throws IllegalStateException إذا كان أحد نطاقات المجموعة مسجّلاً كموقع منفرد.
     */
    fun saveGroup(group: BlockGroup) {
        val conflictSite = getSites().firstOrNull { it.domain in group.domains }
        if (conflictSite != null) {
            throw IllegalStateException(
                "النطاق '${conflictSite.domain}' مسجّل كموقع منفرد — " +
                "أزله أولاً قبل إضافته إلى مجموعة."
            )
        }
        val list = getGroups().toMutableList()
        val idx  = list.indexOfFirst { it.name == group.name }
        if (idx >= 0) list[idx] = group else list.add(group)
        prefs.edit().putString(KEY_GROUPS, serializeGroups(list)).apply()
    }

    /** يُرجع قائمة كل المجموعات المحفوظة. */
    fun getGroups(): List<BlockGroup> {
        val json = prefs.getString(KEY_GROUPS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i -> deserializeGroup(arr.getJSONObject(i)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** يحذف المجموعة بالاسم (لا يفعل شيئاً إن لم تُوجد). */
    fun deleteGroup(name: String) {
        val list = getGroups().filter { it.name != name }
        prefs.edit().putString(KEY_GROUPS, serializeGroups(list)).apply()
    }

    // ── Sites ─────────────────────────────────────────────────────────────────

    /**
     * يحفظ موقعاً منفرداً (إنشاء أو تعديل حسب النطاق).
     * @throws IllegalStateException إذا كان النطاق موجوداً داخل مجموعة.
     */
    fun saveSite(site: BlockSite) {
        val conflictGroup = getGroups().firstOrNull { site.domain in it.domains }
        if (conflictGroup != null) {
            throw IllegalStateException(
                "النطاق '${site.domain}' موجود داخل مجموعة '${conflictGroup.name}' — " +
                "لا يمكن إضافته كموقع منفرد."
            )
        }
        val list = getSites().toMutableList()
        val idx  = list.indexOfFirst { it.domain == site.domain }
        if (idx >= 0) list[idx] = site else list.add(site)
        prefs.edit().putString(KEY_SITES, serializeSites(list)).apply()
    }

    /** يُرجع قائمة كل المواقع المنفردة المحفوظة. */
    fun getSites(): List<BlockSite> {
        val json = prefs.getString(KEY_SITES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i -> deserializeSite(arr.getJSONObject(i)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** يحذف الموقع المنفرد بالنطاق (لا يفعل شيئاً إن لم يُوجد). */
    fun deleteSite(domain: String) {
        val list = getSites().filter { it.domain != domain }
        prefs.edit().putString(KEY_SITES, serializeSites(list)).apply()
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    /**
     * يبحث عن [domain] في المجموعات أولاً ثم في المواقع المنفردة.
     * @return [BlockMatch] إذا كان النطاق خاضعاً لقيد، أو null إذا كان حراً.
     */
    fun findDomain(domain: String): BlockMatch? {
        // ① مطابقة تامة
        getGroups().firstOrNull { domain in it.domains }
            ?.let { return BlockMatch.GroupMatch(it) }
        getSites().firstOrNull { it.domain == domain }
            ?.let { return BlockMatch.SiteMatch(it) }

        // ② مطابقة بالنطاق الأب: m.youtube.com → youtube.com
        // يحل مشكلة المتصفحات التي تفتح النسخة المحمولة على نطاق فرعي
        val parts = domain.split(".")
        for (i in 1 until parts.size - 1) {
            val parent = parts.drop(i).joinToString(".")
            getGroups().firstOrNull { parent in it.domains }
                ?.let { return BlockMatch.GroupMatch(it) }
            getSites().firstOrNull { it.domain == parent }
                ?.let { return BlockMatch.SiteMatch(it) }
        }

        return null
    }

    // ══════════════════════════════════════════════════════════════════════════
    // JSON — Serialization
    // ══════════════════════════════════════════════════════════════════════════

    private fun serializeSchedule(s: BlockSchedule): JSONObject = JSONObject().apply {
        put("days", JSONArray().also { arr -> s.days.forEach { arr.put(it) } })
        put("from", s.from)
        put("to",   s.to)
    }

    private fun serializeLimits(limits: Map<String, Int>): JSONObject =
        JSONObject().apply { limits.forEach { (day, mins) -> put(day, mins) } }

    private fun serializeGroup(g: BlockGroup): JSONObject = JSONObject().apply {
        put("name",        g.name)
        put("domains",     JSONArray().also { arr -> g.domains.forEach  { arr.put(it) } })
        put("dailyLimits", serializeLimits(g.dailyLimits))
        put("schedules",   JSONArray().also { arr -> g.schedules.forEach { arr.put(serializeSchedule(it)) } })
    }

    private fun serializeSite(s: BlockSite): JSONObject = JSONObject().apply {
        put("domain",      s.domain)
        put("dailyLimits", serializeLimits(s.dailyLimits))
        put("schedules",   JSONArray().also { arr -> s.schedules.forEach { arr.put(serializeSchedule(it)) } })
    }

    private fun serializeGroups(list: List<BlockGroup>): String =
        JSONArray().also { arr -> list.forEach { arr.put(serializeGroup(it)) } }.toString()

    private fun serializeSites(list: List<BlockSite>): String =
        JSONArray().also { arr -> list.forEach { arr.put(serializeSite(it)) } }.toString()

    // ══════════════════════════════════════════════════════════════════════════
    // JSON — Deserialization
    // ══════════════════════════════════════════════════════════════════════════

    private fun deserializeSchedule(obj: JSONObject): BlockSchedule {
        val daysArr = obj.getJSONArray("days")
        return BlockSchedule(
            days = List(daysArr.length()) { i -> daysArr.getString(i) },
            from = obj.getString("from"),
            to   = obj.getString("to")
        )
    }

    private fun deserializeLimits(obj: JSONObject): Map<String, Int> =
        mutableMapOf<String, Int>().also { map ->
            obj.keys().forEach { key -> map[key] = obj.getInt(key) }
        }

    private fun deserializeSchedules(arr: JSONArray): List<BlockSchedule> =
        List(arr.length()) { i -> deserializeSchedule(arr.getJSONObject(i)) }

    private fun deserializeGroup(obj: JSONObject): BlockGroup {
        val domainsArr = obj.getJSONArray("domains")
        return BlockGroup(
            name        = obj.getString("name"),
            domains     = List(domainsArr.length()) { i -> domainsArr.getString(i) },
            dailyLimits = deserializeLimits(obj.getJSONObject("dailyLimits")),
            schedules   = deserializeSchedules(obj.getJSONArray("schedules"))
        )
    }

    private fun deserializeSite(obj: JSONObject): BlockSite =
        BlockSite(
            domain      = obj.getString("domain"),
            dailyLimits = deserializeLimits(obj.getJSONObject("dailyLimits")),
            schedules   = deserializeSchedules(obj.getJSONArray("schedules"))
        )
}
