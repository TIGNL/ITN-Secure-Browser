package com.itn.securebrowser

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import com.itn.securebrowser.ui.sheets.BrowserScreen
import com.itn.securebrowser.ui.sheets.Sheet
import com.itn.securebrowser.ui.sheets.SheetHost
import com.itn.securebrowser.ui.sheets.SheetStack
import com.itn.securebrowser.ui.theme.ITNSecureBrowserTheme

class MainActivity : BaseActivity() {

    // ── WebView ─────────────────────────────────────────────────────────────
    lateinit var webViewContainer: FrameLayout
        private set

    // ── Core logic state ─────────────────────────────────────────────────────
    private lateinit var timeTracker: TimeTracker
    private lateinit var blockDataStore: BlockDataStore
    private lateinit var blockEngine: BlockEngine
    private val tabs = mutableListOf<BrowserTab>()
    private var currentTabId = -1
    private var nextTabId = 0
    private var isLoadingInternal = false

    val isDesktopMode: Boolean get() = _isDesktopMode
    private var _isDesktopMode = false

    private val desktopUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36"

    // ── Compose-observable state ─────────────────────────────────────────────
    var url by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var loadProgress by mutableIntStateOf(0)
        private set
    var urlFocused by mutableStateOf(false)
    var focusRequestToken by mutableIntStateOf(0)
    val sheetStack = SheetStack()
    val urlFocusRequester = FocusRequester()

    // ── Block checker (every 5 s) ────────────────────────────────────────────
    private val blockHandler = Handler(Looper.getMainLooper())
    private val blockRunnable = object : Runnable {
        override fun run() {
            periodicBlockCheck()
            blockHandler.postDelayed(this, 5_000L)
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        webViewContainer = FrameLayout(this)
        timeTracker   = TimeTracker(this)
        blockDataStore = BlockDataStore(this)
        blockEngine    = BlockEngine(blockDataStore, timeTracker)

        setContent {
            ITNSecureBrowserTheme {
                SheetHost(stack = sheetStack.stack) { sheet, isTop, dismiss ->
                    com.itn.securebrowser.ui.sheets.BrowserSheetContent(
                        sheet = sheet,
                        stack = sheetStack.stack,
                        activity = this@MainActivity,
                        isTop = isTop,
                        dismiss = dismiss
                    )
                }
                BrowserScreen(activity = this)
            }
        }

        setupBackHandler()
        createNewTab()
    }

    override fun onPause() {
        super.onPause()
        blockHandler.removeCallbacks(blockRunnable)
        timeTracker.onAppPaused()
    }

    override fun onResume() {
        super.onResume()
        timeTracker.onAppResumed(TimeTracker.extractDomain(getCurrentWebView()?.url))
        blockHandler.post(blockRunnable)
    }

    override fun onDestroy() {
        tabs.forEach { tab ->
            webViewContainer.removeView(tab.webView)
            tab.webView.destroy()
        }
        tabs.clear()
        super.onDestroy()
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Back handler — sheet stack first, then WebView back
    // ══════════════════════════════════════════════════════════════════════════

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (sheetStack.stack.isNotEmpty()) {
                    sheetStack.pop()
                    return
                }
                val wv = getCurrentWebView()
                if (wv != null && wv.canGoBack()) {
                    wv.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tab Management
    // ══════════════════════════════════════════════════════════════════════════

    fun createNewTab(url: String = "") {
        val webView = buildWebView()
        val id = nextTabId++
        val tab = BrowserTab(id, webView)
        tabs.add(tab)

        webViewContainer.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        switchToTab(id)
        if (url.isNotEmpty()) webView.loadUrl(url)
    }

    fun switchToTab(id: Int) {
        tabs.forEach { tab ->
            tab.webView.visibility = if (tab.id == id) android.view.View.VISIBLE else android.view.View.GONE
        }
        currentTabId = id
        updateUrlBar(getCurrentWebView()?.url ?: "")
        timeTracker.onDomainChanged(TimeTracker.extractDomain(getCurrentWebView()?.url))
    }

    fun closeTab(id: Int) {
        val index = tabs.indexOfFirst { it.id == id }
        if (index == -1) return
        val tab = tabs[index]
        webViewContainer.removeView(tab.webView)
        tab.webView.destroy()
        tabs.removeAt(index)

        when {
            tabs.isEmpty() -> createNewTab()
            id == currentTabId -> {
                val newIndex = if (index >= tabs.size) tabs.size - 1 else index
                switchToTab(tabs[newIndex].id)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Navigation
    // ══════════════════════════════════════════════════════════════════════════

    fun navigateTo(input: String) {
        if (input.isBlank()) return
        val webViewUrl = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
        }
        val domain = TimeTracker.extractDomain(webViewUrl)
        if (domain != null) {
            val reason = blockEngine.check(domain)
            if (reason != null) {
                getCurrentWebView()?.loadDataWithBaseURL(
                    null, BrowserWebViewClient.buildBlockPage(domain, reason),
                    "text/html", "UTF-8", null
                )
                return
            }
        }
        getCurrentWebView()?.loadUrl(webViewUrl)
    }

    // ── Public action methods called from BrowserScreen ──────────────────────

    fun goBack() { getCurrentWebView()?.let { if (it.canGoBack()) it.goBack() } }
    fun goForward() { getCurrentWebView()?.let { if (it.canGoForward()) it.goForward() } }
    fun canGoBack() = getCurrentWebView()?.canGoBack() == true
    fun canGoForward() = getCurrentWebView()?.canGoForward() == true
    fun goHome() { hideKeyboard(); createNewTab() }
    fun showTabs() { sheetStack.push(Sheet.Tabs(tabs.toList(), currentTabId)) }
    fun showMore() { sheetStack.push(Sheet.More) }

    fun toggleRefreshStop() {
        getCurrentWebView()?.let { wv -> if (isLoading) wv.stopLoading() else wv.reload() }
    }

    fun setDesktopMode(enabled: Boolean) {
        _isDesktopMode = enabled
        tabs.forEach { applyUserAgent(it.webView) }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // WebView Construction
    // ══════════════════════════════════════════════════════════════════════════

    private fun buildWebView(): WebView {
        val wv = WebView(this)

        wv.settings.apply {
            javaScriptEnabled      = true
            domStorageEnabled      = true
            loadWithOverviewMode   = true
            useWideViewPort        = true
            builtInZoomControls    = true
            displayZoomControls    = false
            setSupportZoom(true)
            setSupportMultipleWindows(false)
            cacheMode              = WebSettings.LOAD_DEFAULT
            @Suppress("DEPRECATION")
            allowFileAccess        = true
        }

        applyUserAgent(wv)
        wv.isScrollbarFadingEnabled = true

        wv.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (urlFocused) {
                    hideKeyboard()
                    return@setOnTouchListener true
                }
                val webViewUrl = wv.url
                if (webViewUrl.isNullOrBlank() || webViewUrl == "about:blank") {
                    focusRequestToken++
                    return@setOnTouchListener true
                }
            }
            val webViewUrl = wv.url
            webViewUrl.isNullOrBlank() || webViewUrl == "about:blank" || urlFocused
        }

        wv.webViewClient = BrowserWebViewClient(
            blockEngine    = blockEngine,
            onPageStarted  = { url -> handlePageStarted(wv, url) },
            onPageFinished = { url -> handlePageFinished(wv, url) }
        )
        wv.webChromeClient = BrowserChromeClient(
            onTitleReceived  = { title    -> handleTitleReceived(wv, title) },
            onProgressChanged = { p -> handleProgressChanged(wv, p) }
        )

        wv.visibility = android.view.View.GONE
        return wv
    }

    fun applyUserAgent(webView: WebView) {
        webView.settings.userAgentString = if (_isDesktopMode) desktopUserAgent else null
    }

    // ══════════════════════════════════════════════════════════════════════════
    // WebView Callbacks
    // ══════════════════════════════════════════════════════════════════════════

    private fun handlePageStarted(webView: WebView, pageUrl: String) {
        if (webView != getCurrentWebView()) return
        isLoading = true
        updateUrlBar(pageUrl)
        loadProgress = 0
        timeTracker.onDomainChanged(null)
    }

    private fun handlePageFinished(webView: WebView, pageUrl: String) {
        if (webView != getCurrentWebView()) return
        isLoading = false
        updateUrlBar(pageUrl)
        timeTracker.onDomainChanged(TimeTracker.extractDomain(pageUrl))
    }

    private fun handleTitleReceived(webView: WebView, title: String) {
        tabs.find { it.webView == webView }?.let { it.title = title }
    }

    private fun handleProgressChanged(webView: WebView, p: Int) {
        if (webView == getCurrentWebView()) loadProgress = p
    }

    // ── Periodic block check ─────────────────────────────────────────────────

    private fun periodicBlockCheck() {
        val wv = getCurrentWebView() ?: return
        val webViewUrl = wv.url?.takeIf { it.isNotBlank() && it != "about:blank" } ?: return
        val domain = TimeTracker.extractDomain(webViewUrl) ?: return
        val reason = blockEngine.check(domain) ?: return
        wv.loadDataWithBaseURL(
            null, BrowserWebViewClient.buildBlockPage(domain, reason),
            "text/html", "UTF-8", null
        )
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private fun updateUrlBar(pageUrl: String) {
        if (urlFocused) return
        url = if (pageUrl == "about:blank" || pageUrl.isEmpty()) "" else pageUrl
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        currentFocus?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
    }

    fun getCurrentTab() = tabs.find { it.id == currentTabId }
    fun getCurrentWebView() = getCurrentTab()?.webView
}
