package com.itn.securebrowser

import android.os.Bundle

class ManageSpaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PinManager.hasPin(this)) {
            // يوجد PIN — اطلب التحقق أولاً
            PinEntrySheet(
                mode     = PinEntrySheet.MODE_VERIFY,
                subtitle = getString(R.string.pin_subtitle_manage_data),
                onPinVerified = { openManageSpace() }
            ).show(supportFragmentManager, "pin")
        } else {
            // لا يوجد PIN — افتح مباشرة
            openManageSpace()
        }
    }

    private fun openManageSpace() {
        val sheet = ManageSpaceSheet()
        sheet.show(supportFragmentManager, "manage_space")

        // عند إغلاق الـ sheet أغلق الـ Activity معها
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.fragments.none { it is ManageSpaceSheet }) {
                finish()
            }
        }
        sheet.isCancelable = true
    }

    // إن أغلق المستخدم الـ sheet بالسحب أو Cancel تُغلق الـ Activity
    override fun onResume() {
        super.onResume()
        val hasSheet = supportFragmentManager.fragments.any {
            it is ManageSpaceSheet || it is PinEntrySheet
        }
        if (!hasSheet && !isFinishing) finish()
    }
}
