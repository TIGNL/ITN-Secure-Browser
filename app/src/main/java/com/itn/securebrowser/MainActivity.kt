package com.itn.securebrowser

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.itn.securebrowser.util.LocaleHelper

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BrowserSession.init(this)
        if (BrowserSession.tabs.isEmpty()) BrowserSession.createNewTab(this)
        intent.data?.toString()?.let { BrowserSession.navigateTo(it) }

        setContent {
            ITNBrowserApp(startDestination = "browser")
        }
    }

    override fun onDestroy() {
        BrowserSession.destroy()
        super.onDestroy()
    }
}
