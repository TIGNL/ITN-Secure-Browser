package com.itn.securebrowser

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.itn.securebrowser.util.LocaleHelper
import com.itn.securebrowser.util.PinManager

class ManageSpaceActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val start = if (PinManager.hasPin(this)) {
            pinEntryRoute(
                mode = "verify",
                subtitle = "To manage app data",
                next = "manage_space"
            )
        } else {
            "manage_space"
        }
        setContent {
            ITNBrowserApp(startDestination = start)
        }
    }
}
