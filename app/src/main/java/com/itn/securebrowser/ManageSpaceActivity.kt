package com.itn.securebrowser

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

/**
 * يُستدعى من إعدادات النظام (تطبيقات → ITN → إدارة المساحة)
 * بدلاً من زر "مسح البيانات" الافتراضي.
 * محمي برمز PIN إذا كان مُفعَّلاً.
 */
class ManageSpaceActivity : BaseActivity() {

    private val pinLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) showOptions()
        else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // خلفية داكنة بسيطة تظهر خلف الحوارات
        val bg = View(this).apply { setBackgroundColor(0xFF0F0F1A.toInt()) }
        setContentView(bg)

        if (PinManager.hasPin(this)) {
            pinLauncher.launch(
                PinEntryActivity.intentVerify(this, "لإدارة بيانات التطبيق")
            )
        } else {
            showOptions()
        }
    }

    // ── الخيارات ──────────────────────────────────────────────────────────────

    private fun showOptions() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("إدارة بيانات التطبيق")
            .setItems(
                arrayOf(
                    "مسح الكوكيز والكاش",
                    "مسح سجل الوقت (التتبع)",
                    "مسح بيانات المواقع المحجوبة والجداول",
                    "⚠️  مسح كل شيء"
                )
            ) { _, which ->
                when (which) {
                    0 -> clearBrowsing()
                    1 -> clearTracking()
                    2 -> clearBlocking()
                    3 -> confirmClearAll()
                }
            }
            .setNegativeButton("إلغاء") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    // ── عمليات المسح ──────────────────────────────────────────────────────────

    private fun clearBrowsing() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        cacheDir.deleteRecursively()
        toast("✓ تم مسح الكوكيز والكاش")
        finish()
    }

    private fun clearTracking() {
        getSharedPreferences("itn_time_tracker", MODE_PRIVATE).edit().clear().apply()
        toast("✓ تم مسح سجل الوقت")
        finish()
    }

    private fun clearBlocking() {
        getSharedPreferences("itn_block_data", MODE_PRIVATE).edit().clear().apply()
        toast("✓ تم مسح بيانات الحجب")
        finish()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("⚠️  مسح كل البيانات؟")
            .setMessage(
                "سيتم حذف:\n" +
                "• الكوكيز والكاش\n" +
                "• سجل الوقت والتتبع\n" +
                "• بيانات الحجب والجداول\n" +
                "• الرمز السري\n\n" +
                "لا يمكن التراجع عن هذا الإجراء."
            )
            .setPositiveButton("نعم، امسح كل شيء") { _, _ ->
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                WebStorage.getInstance().deleteAllData()
                cacheDir.deleteRecursively()
                getSharedPreferences("itn_time_tracker", MODE_PRIVATE).edit().clear().apply()
                getSharedPreferences("itn_block_data",   MODE_PRIVATE).edit().clear().apply()
                PinManager.clear(this)
                toast("✓ تم مسح كل البيانات")
                finish()
            }
            .setNegativeButton("إلغاء") { _, _ -> finish() }
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
