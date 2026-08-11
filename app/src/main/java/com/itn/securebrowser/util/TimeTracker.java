package com.itn.securebrowser.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimeTracker {

    private final SharedPreferences prefs;
    private String trackedDomain;
    private long sessionStart;

    public TimeTracker(Context context) {
        prefs = context.getSharedPreferences("itn_time_tracker", Context.MODE_PRIVATE);
    }

    public void onDomainChanged(String newDomain) {
        commitCurrentSession();
        trackedDomain = newDomain;
        sessionStart = (newDomain != null) ? now() : 0L;
    }

    public void onAppPaused() {
        commitCurrentSession();
    }

    public void onAppResumed(String domain) {
        trackedDomain = domain;
        sessionStart = (domain != null) ? now() : 0L;
    }

    public long getTodaySeconds(String domain) {
        long stored = prefs.getLong(key(domain), 0L);
        long live = (domain.equals(trackedDomain) && sessionStart > 0L) ? now() - sessionStart : 0L;
        return stored + live;
    }

    private void commitCurrentSession() {
        if (trackedDomain == null || sessionStart <= 0L) return;
        long elapsed = now() - sessionStart;
        if (elapsed <= 0L) return;
        String k = key(trackedDomain);
        prefs.edit().putLong(k, prefs.getLong(k, 0L) + elapsed).apply();
        sessionStart = 0L;
        trackedDomain = null;
    }

    private String key(String domain) {
        return domain + "_" + todayString();
    }

    private String todayString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private long now() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String extractDomain(String url) {
        if (url == null || url.trim().isEmpty() || "about:blank".equals(url)) return null;
        try {
            String host = URI.create(url).getHost();
            if (host == null || host.isEmpty()) return null;
            if (host.startsWith("www.")) host = host.substring(4);
            return host.isEmpty() ? null : host;
        } catch (Exception e) {
            return null;
        }
    }
}
