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
                PinEntryActivity.intentVerify(this, getString(R.string.pin_subtitle_manage_data))
            )
        } else {
            showOptions()
        }
    }

    // ── الخيارات ──────────────────────────────────────────────────────────────

    private fun showOptions() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(getString(R.string.manage_space_title))
            .setItems(
                arrayOf(
                    getString(R.string.clear_browsing),
                    getString(R.string.clear_tracking),
                    getString(R.string.clear_blocking),
                    getString(R.string.clear_all)
                )
            ) { _, which ->
                when (which) {
                    0 -> clearBrowsing()
                    1 -> clearTracking()
                    2 -> clearBlocking()
                    3 -> confirmClearAll()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    // ── عمليات المسح ──────────────────────────────────────────────────────────

    private fun clearBrowsing() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        cacheDir.deleteRecursively()
        toast(getString(R.string.toast_cleared_browsing))
        finish()
    }

    private fun clearTracking() {
        getSharedPreferences("itn_time_tracker", MODE_PRIVATE).edit().clear().apply()
        toast(getString(R.string.toast_cleared_tracking))
        finish()
    }

    private fun clearBlocking() {
        getSharedPreferences("itn_block_data", MODE_PRIVATE).edit().clear().apply()
        toast(getString(R.string.toast_cleared_blocking))
        finish()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(getString(R.string.clear_all_title))
            .setMessage(
                "سيتم حذف:\n" +
                "• الكوكيز والكاش\n" +
                "• سجل الوقت والتتبع\n" +
                "• بيانات الحجب والجداول\n" +
                "• الرمز السري\n\n" +
                "لا يمكن التراجع عن هذا الإجراء."
            )
            .setPositiveButton(getString(R.string.btn_clear_all_confirm)) { _, _ ->
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                WebStorage.getInstance().deleteAllData()
                cacheDir.deleteRecursively()
                getSharedPreferences("itn_time_tracker", MODE_PRIVATE).edit().clear().apply()
                getSharedPreferences("itn_block_data",   MODE_PRIVATE).edit().clear().apply()
                PinManager.clear(this)
                toast(getString(R.string.toast_cleared_all))
                finish()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { _, _ -> finish() }
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
