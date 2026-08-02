package com.itn.securebrowser

import android.content.Context
import java.security.MessageDigest

/**
 * مدير الرمز السري — يخزّن hash بتشفير SHA-256 فقط، لا يُخزَّن الرمز نصاً.
 */
object PinManager {

    private const val PREFS   = "itn_pin"
    private const val KEY_PIN = "pin_hash"

    /** هل يوجد رمز سري محفوظ؟ */
    fun hasPin(ctx: Context): Boolean =
        prefs(ctx).getString(KEY_PIN, null) != null

    /** حفظ رمز سري جديد (يُشفَّر قبل الحفظ) */
    fun savePin(ctx: Context, pin: String) {
        prefs(ctx).edit().putString(KEY_PIN, hash(pin)).apply()
    }

    /** التحقق من رمز مُدخَل */
    fun verify(ctx: Context, pin: String): Boolean =
        prefs(ctx).getString(KEY_PIN, null) == hash(pin)

    /** حذف الرمز السري */
    fun clear(ctx: Context) {
        prefs(ctx).edit().remove(KEY_PIN).apply()
    }

    // ── Private ────────────────────────────────────────────────────────────

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
