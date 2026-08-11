package com.itn.securebrowser.util;

import android.webkit.WebView;

public class BrowserTab {
    public final int id;
    public final WebView webView;
    public String title = "New Tab";
    public String url = "";

    public BrowserTab(int id, WebView webView) {
        this.id = id;
        this.webView = webView;
    }
}
