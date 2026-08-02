package com.itn.securebrowser

import android.webkit.WebChromeClient
import android.webkit.WebView

class BrowserChromeClient(
    private val onTitleReceived: (title: String) -> Unit,
    private val onProgressChanged: (progress: Int) -> Unit
) : WebChromeClient() {

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        onTitleReceived(title)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged(newProgress)
    }
}
