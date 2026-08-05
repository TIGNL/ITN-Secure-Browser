package com.itn.securebrowser

import android.os.Bundle

class ManageSpaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PinManager.hasPin(this)) {
            PinEntrySheet(
                mode     = PinEntrySheet.MODE_VERIFY,
                subtitle = getString(R.string.pin_subtitle_manage_data),
                onPinVerified = { openManageSpace() }
            ).also { sheet ->
                sheet.show(supportFragmentManager, "pin")
                // إن أغلق الـ PIN sheet بدون تحقق أغلق الـ Activity
                supportFragmentManager.setFragmentResultListener("pin_dismissed", this) { _, _ ->
                    if (!isFinishing) finish()
                }
            }
        } else {
            openManageSpace()
        }
    }

    private fun openManageSpace() {
        ManageSpaceSheet(
            onDismissed = { if (!isFinishing) finish() }
        ).show(supportFragmentManager, "manage_space")
    }
}
