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
            ).show(supportFragmentManager, "pin")
        } else {
            openManageSpace()
        }
    }

    private fun openManageSpace() {
        val sheet = ManageSpaceSheet()
        sheet.show(supportFragmentManager, "manage_space")

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.fragments.none { it is ManageSpaceSheet }) {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val hasSheet = supportFragmentManager.fragments.any {
            it is ManageSpaceSheet || it is PinEntrySheet
        }
        if (!hasSheet && !isFinishing) finish()
    }
}
