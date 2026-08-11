package com.itn.securebrowser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import com.itn.securebrowser.util.BlockDataStore;
import com.itn.securebrowser.util.BlockEngine;
import com.itn.securebrowser.util.BrowserTab;
import com.itn.securebrowser.util.PinManager;
import com.itn.securebrowser.util.TimeTracker;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static final List<BrowserTab> tabs = new ArrayList<>();
    public static int currentTabId = -1;
    private static int nextTabId = 0;

    private FrameLayout webViewContainer;
    private EditText urlBar;
    private ImageButton btnRefreshStop;
    private ProgressBar progressBar;
    private ImageButton btnBack, btnForward, btnHome, btnTabs, btnMore;

    private TimeTracker timeTracker;
    private BlockDataStore blockDataStore;
    private BlockEngine blockEngine;

    private boolean isLoading = false;
    public boolean isDesktopMode = false;

    private final String desktopUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36";

    private final Handler blockHandler = new Handler(Looper.getMainLooper());
    private final Runnable blockRunnable = new Runnable() {
        @Override public void run() {
            periodicBlockCheck();
            blockHandler.postDelayed(this, 5000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webViewContainer = findViewById(R.id.webViewContainer);
        urlBar = findViewById(R.id.urlBar);
        btnRefreshStop = findViewById(R.id.btnRefreshStop);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnForward = findViewById(R.id.btnForward);
        btnHome = findViewById(R.id.btnHome);
        btnTabs = findViewById(R.id.btnTabs);
        btnMore = findViewById(R.id.btnMore);

        timeTracker = new TimeTracker(this);
        blockDataStore = new BlockDataStore(this);
        blockEngine = new BlockEngine(blockDataStore, timeTracker);

        btnRefreshStop.setOnClickListener(v -> toggleRefreshStop());
        btnBack.setOnClickListener(v -> goBack());
        btnForward.setOnClickListener(v -> goForward());
        btnHome.setOnClickListener(v -> goHome());
        btnTabs.setOnClickListener(v -> startActivity(new Intent(this, TabListActivity.class)));
        btnMore.setOnClickListener(v -> {
            if (PinManager.hasPin(this)) {
                startActivity(new Intent(this, PinEntryActivity.class)
                    .putExtra("mode", "verify")
                    .putExtra("subtitle", "To open settings")
                    .putExtra("next", "settings"));
            } else {
                startActivity(new Intent(this, SettingsActivity.class));
            }
        });

        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                navigateTo(urlBar.getText().toString().trim());
                hideKeyboard();
                return true;
            }
            return false;
        });

        urlBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                urlBar.selectAll();
            }
        });

        if (tabs.isEmpty()) {
            createNewTab("");
        } else {
            for (BrowserTab tab : tabs) {
                webViewContainer.addView(tab.webView);
            }
            switchToTab(currentTabId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebView wv = getCurrentWebView();
        timeTracker.onAppResumed(wv != null ? TimeTracker.extractDomain(wv.getUrl()) : null);
        blockHandler.post(blockRunnable);
        updateNavButtons();
    }

    @Override
    protected void onPause() {
        super.onPause();
        blockHandler.removeCallbacks(blockRunnable);
        timeTracker.onAppPaused();
    }

    @Override
    protected void onDestroy() {
        for (BrowserTab tab : tabs) {
            webViewContainer.removeView(tab.webView);
            tab.webView.destroy();
        }
        tabs.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        WebView wv = getCurrentWebView();
        if (wv != null && wv.canGoBack()) {
            wv.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // --- Tab Management ---

    public void createNewTab(String url) {
        WebView webView = buildWebView();
        int id = nextTabId++;
        BrowserTab tab = new BrowserTab(id, webView);
        tabs.add(tab);

        webViewContainer.addView(webView,
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        switchToTab(id);
        if (url != null && !url.isEmpty()) webView.loadUrl(url);
    }

    public void switchToTab(int id) {
        for (BrowserTab tab : tabs) {
            tab.webView.setVisibility(tab.id == id ? View.VISIBLE : View.GONE);
        }
        currentTabId = id;
        WebView wv = getCurrentWebView();
        if (wv != null) {
            updateUrlBar(wv.getUrl());
            timeTracker.onDomainChanged(TimeTracker.extractDomain(wv.getUrl()));
        }
        updateNavButtons();
    }

    public void closeTab(int id) {
        int index = -1;
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).id == id) { index = i; break; }
        }
        if (index == -1) return;
        BrowserTab tab = tabs.get(index);
        webViewContainer.removeView(tab.webView);
        tab.webView.destroy();
        tabs.remove(index);

        if (tabs.isEmpty()) {
            createNewTab("");
        } else if (id == currentTabId) {
            int newIndex = Math.min(index, tabs.size() - 1);
            switchToTab(tabs.get(newIndex).id);
        }
    }

    // --- Navigation ---

    public void navigateTo(String input) {
        if (input == null || input.trim().isEmpty()) return;
        String webViewUrl;
        if (input.startsWith("http://") || input.startsWith("https://")) {
            webViewUrl = input;
        } else if (input.contains(".") && !input.contains(" ")) {
            webViewUrl = "https://" + input;
        } else {
            webViewUrl = "https://www.google.com/search?q=" + input.replace(" ", "+");
        }

        String domain = TimeTracker.extractDomain(webViewUrl);
        if (domain != null) {
            BlockEngine.BlockReason reason = blockEngine.check(domain);
            if (reason != null) {
                WebView wv = getCurrentWebView();
                if (wv != null) wv.loadDataWithBaseURL(null,
                    BlockEngine.buildBlockPage(domain, reason), "text/html", "UTF-8", null);
                return;
            }
        }
        WebView wv = getCurrentWebView();
        if (wv != null) wv.loadUrl(webViewUrl);
    }

    private void goBack() {
        WebView wv = getCurrentWebView();
        if (wv != null && wv.canGoBack()) wv.goBack();
    }

    private void goForward() {
        WebView wv = getCurrentWebView();
        if (wv != null && wv.canGoForward()) wv.goForward();
    }

    private void goHome() {
        hideKeyboard();
        createNewTab("");
    }

    private void toggleRefreshStop() {
        WebView wv = getCurrentWebView();
        if (wv != null) {
            if (isLoading) wv.stopLoading(); else wv.reload();
        }
    }

    public void setDesktopMode(boolean enabled) {
        isDesktopMode = enabled;
        for (BrowserTab tab : tabs) {
            tab.webView.getSettings().setUserAgentString(enabled ? desktopUserAgent : null);
        }
    }

    // --- WebView ---

    private WebView buildWebView() {
        WebView wv = new WebView(this);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setSupportMultipleWindows(false);
        wv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        wv.setScrollbarFadingEnabled(true);

        if (isDesktopMode) wv.getSettings().setUserAgentString(desktopUserAgent);

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                String url = request.getUrl().toString();
                String domain = extractDomain(url);
                if (domain != null) {
                    BlockEngine.BlockReason reason = blockEngine.check(domain);
                    if (reason != null) {
                        view.loadDataWithBaseURL(null,
                            BlockEngine.buildBlockPage(domain, reason), "text/html", "UTF-8", null);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (view != getCurrentWebView()) return;
                isLoading = true;
                updateUrlBar(url);
                progressBar.setVisibility(View.VISIBLE);
                timeTracker.onDomainChanged(null);
                updateNavButtons();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (view != getCurrentWebView()) return;
                isLoading = false;
                updateUrlBar(url);
                progressBar.setVisibility(View.GONE);
                timeTracker.onDomainChanged(TimeTracker.extractDomain(url));
                updateNavButtons();
                for (BrowserTab tab : tabs) {
                    if (tab.webView == view) {
                        tab.title = view.getTitle() != null ? view.getTitle() : "New Tab";
                        tab.url = url != null ? url : "";
                    }
                }
            }
        });

        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (view == getCurrentWebView()) progressBar.setProgress(newProgress);
            }
        });

        wv.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                WebView cwv = getCurrentWebView();
                if (cwv != null) {
                    String u = cwv.getUrl();
                    if (u == null || u.isEmpty() || "about:blank".equals(u)) {
                        urlBar.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        wv.setVisibility(View.GONE);
        return wv;
    }

    // --- Helpers ---

    private void updateUrlBar(String pageUrl) {
        if (urlBar.hasFocus()) return;
        if (pageUrl == null || "about:blank".equals(pageUrl) || pageUrl.isEmpty()) {
            urlBar.setText("");
        } else {
            urlBar.setText(pageUrl);
        }
    }

    private void updateNavButtons() {
        WebView wv = getCurrentWebView();
        btnBack.setEnabled(wv != null && wv.canGoBack());
        btnForward.setEnabled(wv != null && wv.canGoForward());
        btnBack.setAlpha(wv != null && wv.canGoBack() ? 1.0f : 0.3f);
        btnForward.setAlpha(wv != null && wv.canGoForward() ? 1.0f : 0.3f);
        btnRefreshStop.setImageResource(isLoading ? R.drawable.ic_stop : R.drawable.ic_refresh);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public WebView getCurrentWebView() {
        for (BrowserTab tab : tabs) {
            if (tab.id == currentTabId) return tab.webView;
        }
        return null;
    }

    private void periodicBlockCheck() {
        WebView wv = getCurrentWebView();
        if (wv == null) return;
        String url = wv.getUrl();
        if (url == null || url.isEmpty() || "about:blank".equals(url)) return;
        String domain = TimeTracker.extractDomain(url);
        if (domain == null) return;
        BlockEngine.BlockReason reason = blockEngine.check(domain);
        if (reason != null) {
            wv.loadDataWithBaseURL(null,
                BlockEngine.buildBlockPage(domain, reason), "text/html", "UTF-8", null);
        }
    }

    private String extractDomain(String url) {
        if (url == null || url.startsWith("data:") || url.startsWith("about:")) return null;
        try {
            String host = Uri.parse(url).getHost();
            if (host == null) return null;
            if (host.startsWith("www.")) host = host.substring(4);
            return host.isEmpty() ? null : host;
        } catch (Exception e) {
            return null;
        }
    }
}
