package com.itn.securebrowser

import android.os.Bundle

class ManageSpaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ManageSpaceSheet().show(supportFragmentManager, "manage_space")
    }
}
