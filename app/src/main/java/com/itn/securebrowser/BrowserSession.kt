package com.itn.securebrowser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.MotionEvent
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.itn.securebrowser.util.BlockDataStore
import com.itn.securebrowser.util.BlockEngine
import com.itn.securebrowser.util.TimeTracker

class BrowserTab(val id: Int, val webView: WebView) {
    var title by mutableStateOf("New Tab")
        internal set
    var url by mutableStateOf("")
        internal set
}

object BrowserSession {
    val tabs = mutableStateListOf<BrowserTab>()
    var currentTabId by mutableIntStateOf(-1)
        private set

    val urlText = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val progress = mutableIntStateOf(0)
    val canGoBack = mutableStateOf(false)
    val canGoForward = mutableStateOf(false)

    var isDesktopMode = false
        private set
    var urlEditing = false
    var onBlankTap: (() -> Unit)? = null

    private var nextTabId = 0
    private var blockEngine: BlockEngine? = null
    private var timeTracker: TimeTracker? = null

    private val desktopUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36"

    fun init(context: Context) {
        if (blockEngine != null) return
        val app = context.applicationContext
        timeTracker = TimeTracker(app)
        blockEngine = BlockEngine(BlockDataStore(app), timeTracker)
    }

    fun currentTab(): BrowserTab? = tabs.firstOrNull { it.id == currentTabId }

    fun currentWebView(): WebView? = currentTab()?.webView

    fun createNewTab(context: Context, url: String = "") {
        val webView = buildWebView(context.applicationContext)
        val tab = BrowserTab(nextTabId++, webView)
        tabs.add(tab)
        switchToTab(tab.id)
        if (url.isNotEmpty()) webView.loadUrl(url)
    }

    fun switchToTab(id: Int) {
        val tab = tabs.firstOrNull { it.id == id } ?: return
        currentTabId = id
        updateUrl(tab.url)
        timeTracker?.onDomainChanged(extractDomain(tab.url))
        updateNav()
    }

    fun closeTab(context: Context, id: Int) {
        val index = tabs.indexOfFirst { it.id == id }
        if (index < 0) return
        val tab = tabs[index]
        tab.webView.parent?.let { (it as? android.view.ViewGroup)?.removeView(tab.webView) }
        tab.webView.destroy()
        tabs.removeAt(index)

        if (tabs.isEmpty()) {
            createNewTab(context)
        } else if (id == currentTabId) {
            val next = tabs[minOf(index, tabs.lastIndex)]
            switchToTab(next.id)
        }
    }

    fun navigateTo(input: String) {
        val value = input.trim()
        if (value.isEmpty()) return
        val url = resolveUrl(value)
        val domain = extractDomain(url)
        if (domain != null) {
            val reason = blockEngine?.check(domain)
            if (reason != null) {
                loadBlockedPage(currentWebView(), domain, reason)
                return
            }
        }
        currentWebView()?.loadUrl(url)
    }

    fun goBack() {
        currentWebView()?.let { if (it.canGoBack()) it.goBack() }
    }

    fun goForward() {
        currentWebView()?.let { if (it.canGoForward()) it.goForward() }
    }

    fun reloadOrStop() {
        val view = currentWebView() ?: return
        if (isLoading.value) view.stopLoading() else view.reload()
    }

    fun setDesktopMode(enabled: Boolean) {
        isDesktopMode = enabled
        tabs.forEach { tab ->
            tab.webView.settings.userAgentString = if (enabled) desktopUserAgent else null
        }
    }

    fun onAppResumed() {
        timeTracker?.onAppResumed(extractDomain(currentWebView()?.url))
    }

    fun onAppPaused() {
        timeTracker?.onAppPaused()
    }

    fun periodicBlockCheck() {
        val url = currentWebView()?.url ?: return
        if (url.isEmpty() || url.startsWith("about:") || url.startsWith("data:")) return
        val domain = extractDomain(url) ?: return
        val reason = blockEngine?.check(domain) ?: return
        loadBlockedPage(currentWebView(), domain, reason)
    }

    fun destroy() {
        tabs.forEach { tab ->
            tab.webView.parent?.let { (it as? android.view.ViewGroup)?.removeView(tab.webView) }
            tab.webView.destroy()
        }
        tabs.clear()
        blockEngine = null
        timeTracker = null
    }

    fun extractDomain(url: String?): String? {
        if (url == null || url.startsWith("data:") || url.startsWith("about:")) return null
        return try {
            var host = Uri.parse(url).host ?: return null
            if (host.startsWith("www.")) host = host.substring(4)
            host.ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveUrl(input: String): String = when {
        input.startsWith("http://") || input.startsWith("https://") -> input
        input.contains(".") && !input.contains(" ") -> "https://$input"
        else -> "https://www.google.com/search?q=" + input.replace(" ", "+")
    }

    private fun updateUrl(url: String?) {
        if (urlEditing) return
        urlText.value = when {
            url.isNullOrEmpty() || url == "about:blank" -> ""
            else -> url
        }
    }

    private fun updateNav() {
        val view = currentWebView()
        canGoBack.value = view?.canGoBack() == true
        canGoForward.value = view?.canGoForward() == true
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildWebView(context: Context): WebView {
        val view = WebView(context)
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            setSupportMultipleWindows(false)
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        if (isDesktopMode) view.settings.userAgentString = desktopUserAgent
        view.isScrollbarFadingEnabled = true

        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val target = request?.url?.toString() ?: return false
                val domain = extractDomain(target) ?: return false
                val reason = blockEngine?.check(domain) ?: return false
                loadBlockedPage(view, domain, reason)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (view != currentWebView()) return
                isLoading.value = true
                updateUrl(url)
                progress.value = 0
                timeTracker?.onDomainChanged(null)
                updateNav()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (view != currentWebView()) return
                isLoading.value = false
                updateUrl(url)
                progress.value = 100
                val tab = currentTab()
                if (tab != null && view != null) {
                    tab.title = view.title?.takeIf { it.isNotBlank() } ?: "New Tab"
                    tab.url = url ?: ""
                }
                timeTracker?.onDomainChanged(extractDomain(url))
                updateNav()
            }
        }

        view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (view == currentWebView()) progress.value = newProgress
            }
        }

        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && v is WebView) {
                val current = v.url
                if (current.isNullOrEmpty() || current == "about:blank") {
                    onBlankTap?.invoke()
                    return@setOnTouchListener true
                }
            }
            false
        }

        return view
    }

    private fun loadBlockedPage(view: WebView?, domain: String, reason: BlockEngine.BlockReason) {
        if (view == null) return
        view.loadDataWithBaseURL(
            null,
            BlockEngine.buildBlockPage(domain, reason),
            "text/html",
            "UTF-8",
            null
        )
    }
}
