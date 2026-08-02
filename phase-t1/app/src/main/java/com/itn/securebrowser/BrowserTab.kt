package com.itn.securebrowser

import android.webkit.WebView

data class BrowserTab(
    val id: Int,
    val webView: WebView,
    var title: String = "New Tab",
    var url: String = ""
)
