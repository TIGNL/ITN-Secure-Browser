package com.itn.securebrowser

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.*
import androidx.activity.OnBackPressedCallback

class MainActivity : BaseActivity() {

    // ── Settings PIN launcher ────────────────────────────────────────────────
    private val pinForSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }
    }

    // ── Views ──────────────────────────────────────────────────────────────
    private lateinit var webViewContainer: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var btnRefreshStop: ImageButton

    // Custom nav views
    private lateinit var btnNavGroup: FrameLayout
    private lateinit var triangleNav: TriangleNavView
    private lateinit var btnHome: FrameLayout
    private lateinit var btnTabs: FrameLayout
    private lateinit var tabsSquare: TabsSquareView
    private lateinit var btnMore: ImageButton

    private lateinit var tabsContainer: LinearLayout
    private lateinit var tabsScrollView: HorizontalScrollView
    private lateinit var btnNewTab: ImageButton
    private lateinit var progressBar: ProgressBar

    // ── Block checker (فحص دوري كل 5 ثوانٍ) ────────────────────────────────────
    private val blockHandler  = Handler(Looper.getMainLooper())
    private val blockRunnable = object : Runnable {
        override fun run() {
            periodicBlockCheck()
            blockHandler.postDelayed(this, 5_000L)
        }
    }

    // ── State ──────────────────────────────────────────────────────────────
    private lateinit var timeTracker: TimeTracker
    private lateinit var blockDataStore: BlockDataStore
    private lateinit var blockEngine: BlockEngine
    private val tabs = mutableListOf<BrowserTab>()
    private var currentTabId = -1
    private var nextTabId = 0
    private var isLoading = false
    var isDesktopMode = false

    // isForwardMode: true → المثلث يشير للأمام (يمين)، false → للخلف (يسار)
    private var isForwardMode = false

    private val desktopUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36"

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timeTracker   = TimeTracker(this)
        blockDataStore = BlockDataStore(this)
        blockEngine    = BlockEngine(blockDataStore, timeTracker)
        initViews()
        setupListeners()
        setupBackHandler()
        createNewTab()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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

    // ── Initialisation ─────────────────────────────────────────────────────

    private fun initViews() {
        webViewContainer  = findViewById(R.id.webViewContainer)
        urlBar            = findViewById(R.id.urlBar)
        btnRefreshStop    = findViewById(R.id.btnRefreshStop)
        tabsContainer     = findViewById(R.id.tabsContainer)
        tabsScrollView    = findViewById(R.id.tabsScrollView)
        btnNewTab         = findViewById(R.id.btnNewTab)
        progressBar       = findViewById(R.id.progressBar)

        // Custom nav views
        btnNavGroup  = findViewById(R.id.btnNavGroup)
        triangleNav  = findViewById(R.id.btnBack)
        btnHome      = findViewById(R.id.btnHome)
        btnTabs      = findViewById(R.id.btnTabs)
        tabsSquare   = findViewById(R.id.tabsSquare)
        btnMore      = findViewById(R.id.btnMore)
    }

    private fun setupListeners() {
        // زر الخلف/الأمام — ضغطة قصيرة، ضغطة طويلة
        btnNavGroup.setOnClickListener {
            val wv = getCurrentWebView() ?: return@setOnClickListener
            if (isForwardMode) {
                if (wv.canGoForward()) wv.goForward()
            } else {
                if (wv.canGoBack()) wv.goBack()
            }
        }
        btnNavGroup.setOnLongClickListener {
            // تبديل الوجهة عند الضغط المطوّل
            isForwardMode = !isForwardMode
            updateNavButtons()
            true
        }

        btnRefreshStop.setOnClickListener {
            getCurrentWebView()?.let { wv ->
                if (isLoading) wv.stopLoading() else wv.reload()
            }
        }
        btnNewTab.setOnClickListener { createNewTab() }
        btnHome.setOnClickListener {
            hideKeyboard()
            createNewTab()
        }
        btnTabs.setOnClickListener { showTabsSheet() }
        btnMore.setOnClickListener { showMoreSheet() }

        urlBar.setOnEditorActionListener { _, actionId, event ->
            val isGo    = actionId == EditorInfo.IME_ACTION_GO
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                          event.action  == KeyEvent.ACTION_DOWN
            if (isGo || isEnter) {
                navigateTo(urlBar.text.toString().trim())
                hideKeyboard()
                true
            } else false
        }

        urlBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) urlBar.post { urlBar.selectAll() }
        }
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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

    // ── Tab Management ─────────────────────────────────────────────────────

    private fun createNewTab(url: String = "") {
        val webView = buildWebView()
        val id      = nextTabId++
        val tab     = BrowserTab(id, webView)
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

    private fun switchToTab(id: Int) {
        tabs.forEach { tab ->
            tab.webView.visibility = if (tab.id == id) View.VISIBLE else View.GONE
        }
        currentTabId = id
        updateUrlBar(getCurrentWebView()?.url ?: "")
        updateNavButtons()
        refreshTabBar()
        scrollTabsToActive()
        timeTracker.onDomainChanged(TimeTracker.extractDomain(getCurrentWebView()?.url))
    }

    private fun closeTab(id: Int) {
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
            else -> refreshTabBar()
        }
    }

    private fun refreshTabBar() {
        tabsContainer.removeAllViews()
        tabs.forEach { tab ->
            val tabView  = layoutInflater.inflate(R.layout.item_tab, tabsContainer, false)
            val titleTv  = tabView.findViewById<TextView>(R.id.tabTitle)
            val closeBtn = tabView.findViewById<ImageButton>(R.id.tabClose)

            titleTv.text = tab.title
                .takeIf { it.isNotBlank() && it != "about:blank" }
                ?: "New Tab"

            tabView.setBackgroundResource(
                if (tab.id == currentTabId) R.drawable.tab_active_background
                else R.drawable.tab_inactive_background
            )

            tabView.setOnClickListener  { switchToTab(tab.id) }
            closeBtn.setOnClickListener { closeTab(tab.id) }
            tabsContainer.addView(tabView)
        }

        // تحديث عداد التبويبات في الشريط السفلي
        tabsSquare.tabCount = tabs.size
    }

    private fun scrollTabsToActive() {
        tabsScrollView.post {
            val index = tabs.indexOfFirst { it.id == currentTabId }
            if (index >= 0 && tabsContainer.childCount > index) {
                tabsScrollView.smoothScrollTo(tabsContainer.getChildAt(index).left, 0)
            }
        }
    }

    // ── WebView Construction ───────────────────────────────────────────────

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
                if (urlBar.isFocused) {
                    hideKeyboard()
                    return@setOnTouchListener true
                }
                val url = wv.url
                if (url.isNullOrBlank() || url == "about:blank") {
                    urlBar.requestFocus()
                    urlBar.post { showKeyboard() }
                    return@setOnTouchListener true
                }
            }
            val url = wv.url
            if (url.isNullOrBlank() || url == "about:blank" || urlBar.isFocused) true
            else false
        }

        wv.webViewClient = BrowserWebViewClient(
            blockEngine    = blockEngine,
            onPageStarted  = { url -> handlePageStarted(wv, url) },
            onPageFinished = { url -> handlePageFinished(wv, url) }
        )
        wv.webChromeClient = BrowserChromeClient(
            onTitleReceived  = { title    -> handleTitleReceived(wv, title) },
            onProgressChanged = { progress -> handleProgressChanged(wv, progress) }
        )

        wv.visibility = View.GONE
        return wv
    }

    // ── WebView Callbacks ──────────────────────────────────────────────────

    private fun handlePageStarted(webView: WebView, url: String) {
        if (webView != getCurrentWebView()) return
        isLoading = true
        updateUrlBar(url)
        updateNavButtons()
        btnRefreshStop.setImageResource(R.drawable.ic_stop)
        progressBar.visibility = View.VISIBLE
        timeTracker.onDomainChanged(null)
    }

    private fun handlePageFinished(webView: WebView, url: String) {
        if (webView != getCurrentWebView()) return
        isLoading = false
        updateUrlBar(url)
        updateNavButtons()
        btnRefreshStop.setImageResource(R.drawable.ic_refresh)
        progressBar.visibility = View.GONE
        timeTracker.onDomainChanged(TimeTracker.extractDomain(url))
    }

    private fun handleTitleReceived(webView: WebView, title: String) {
        tabs.find { it.webView == webView }?.let { tab ->
            tab.title = title
            refreshTabBar()
        }
    }

    private fun handleProgressChanged(webView: WebView, progress: Int) {
        if (webView == getCurrentWebView()) progressBar.progress = progress
    }

    // ── فحص دوري للحجب ──────────────────────────────────────────────────────────

    private fun periodicBlockCheck() {
        val wv  = getCurrentWebView() ?: return
        val url = wv.url?.takeIf { it.isNotBlank() && it != "about:blank" } ?: return
        val domain = TimeTracker.extractDomain(url) ?: return
        val reason = blockEngine.check(domain) ?: return
        wv.loadDataWithBaseURL(
            null, BrowserWebViewClient.buildBlockPage(domain, reason),
            "text/html", "UTF-8", null
        )
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    private fun navigateTo(input: String) {
        if (input.isBlank()) return
        val url = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ")                  -> "https://$input"
            else -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
        }
        val domain = TimeTracker.extractDomain(url)
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
        getCurrentWebView()?.loadUrl(url)
    }

    fun applyUserAgent(webView: WebView) {
        webView.settings.userAgentString =
            if (isDesktopMode) desktopUserAgent else null
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────

    private fun updateUrlBar(url: String) {
        if (urlBar.isFocused) return
        urlBar.setText(if (url == "about:blank" || url.isEmpty()) "" else url)
    }

    private fun updateNavButtons() {
        val wv = getCurrentWebView()

        // تحديث شفافية زر التنقل
        val canNav = if (isForwardMode) wv?.canGoForward() == true
                     else               wv?.canGoBack()    == true
        btnNavGroup.alpha = if (canNav) 1.0f else 0.35f

        // عكس المثلث عند الوضع الأمامي
        triangleNav.scaleX = if (isForwardMode) -1f else 1f
    }

    private fun getCurrentTab()     = tabs.find { it.id == currentTabId }
    private fun getCurrentWebView() = getCurrentTab()?.webView

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(urlBar, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(urlBar.windowToken, 0)
        urlBar.clearFocus()
    }

    // ── Tabs Bottom Sheet ──────────────────────────────────────────────────
    private fun showTabsSheet() {
        TabsBottomSheet(
            tabs        = tabs.toList(),
            activeId    = currentTabId,
            onSelect    = { tab -> switchToTab(tab.id) },
            onClose     = { tab -> closeTab(tab.id) },
            onNewTab    = { createNewTab() },
            onNewTabFromHistory = { /* ت-٣: سيُفتح HistoryActivity */ }
        ).show(supportFragmentManager, TabsBottomSheet.TAG)
    }

    // ── More Bottom Sheet ──────────────────────────────────────────────────
    private fun showMoreSheet() {
        MoreBottomSheet(
            isDesktopMode          = isDesktopMode,
            onDesktopModeToggled   = { enabled ->
                isDesktopMode = enabled
                tabs.forEach { applyUserAgent(it.webView) }
            },
            onOpenParentalSettings = {
                if (PinManager.hasPin(this)) {
                    pinForSettingsLauncher.launch(
                        PinEntryActivity.intentVerify(this)
                    )
                } else {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
        ).show(supportFragmentManager, MoreBottomSheet.TAG)
    }

}
