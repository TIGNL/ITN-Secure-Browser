package com.itn.securebrowser

import android.os.Bundle

class ManageSpaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PinManager.hasPin(this)) {
            PinEntrySheet(
                mode          = PinEntrySheet.MODE_VERIFY,
                subtitle      = getString(R.string.pin_subtitle_manage_data),
                onPinVerified = { openManageSpace() },
                onDismissed   = { if (!isFinishing) finish() }
            ).show(supportFragmentManager, "pin")
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
