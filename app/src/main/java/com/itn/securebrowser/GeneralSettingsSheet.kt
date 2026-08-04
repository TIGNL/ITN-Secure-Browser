package com.itn.securebrowser

import android.os.Bundle
import android.view.View

class GeneralSettingsSheet : BaseListSheet() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPageTitle(getString(R.string.general_settings))
    }
}
