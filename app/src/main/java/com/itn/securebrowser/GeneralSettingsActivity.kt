package com.itn.securebrowser

import android.os.Bundle

class GeneralSettingsActivity : BaseListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(getString(R.string.general_settings))
    }
}
