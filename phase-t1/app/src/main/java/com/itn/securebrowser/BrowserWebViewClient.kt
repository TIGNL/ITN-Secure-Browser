package com.itn.securebrowser

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class BrowserWebViewClient(
    private val blockEngine: BlockEngine,
    private val onPageStarted: (url: String) -> Unit,
    private val onPageFinished: (url: String) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url    = request.url.toString()
        val domain = extractDomain(url) ?: return false

        val reason = blockEngine.check(domain)
        if (reason != null) {
            showBlockPage(view, domain, reason)
            return true   // منع التحميل
        }
        return false      // السماح للـ WebView بالتابعة
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        // فحص إضافي عند بداية التحميل (مثلاً إعادة توجيه)
        val domain = extractDomain(url)
        if (domain != null) {
            val reason = blockEngine.check(domain)
            if (reason != null) {
                view.stopLoading()
                showBlockPage(view, domain, reason)
                return
            }
        }
        onPageStarted(url)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onPageFinished(url)
    }

    // ── صفحة الحجب ────────────────────────────────────────────────────────────

    private fun showBlockPage(view: WebView, domain: String, reason: BlockReason) {
        view.loadDataWithBaseURL(null, buildBlockPage(domain, reason), "text/html", "UTF-8", null)
    }

    companion object {
        /** يُستدعى أيضاً من MainActivity للفحص الدوري. */
        fun buildBlockPage(domain: String, reason: BlockReason): String {
            val (title, message, icon) = when (reason) {
                is BlockReason.ScheduleBlock -> Triple(
                    "وقت حظر مجدول",
                    "هذا الموقع محجوب خلال هذا الوقت.",
                    "🕐"
                )
                is BlockReason.LimitReached -> Triple(
                    "انتهى وقتك اليوم",
                    "استهلكت ${reason.usedMinutes} من أصل ${reason.limitMinutes} دقيقة مسموحة.",
                    "⏱️"
                )
            }
            return buildHtml(domain, icon, title, message)
        }

        private fun buildHtml(domain: String, icon: String, title: String, message: String): String = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>محجوب</title>
              <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                body {
                  min-height:100vh;
                  display:flex; align-items:center; justify-content:center;
                  background:#0f0f0f;
                  font-family: 'Segoe UI', Tahoma, sans-serif;
                  color:#e0e0e0;
                }
                .card {
                  background:#1a1a1a;
                  border:1px solid #2a2a2a;
                  border-radius:20px;
                  padding:40px 32px;
                  max-width:360px;
                  width:90%;
                  text-align:center;
                  box-shadow:0 8px 32px rgba(0,0,0,0.5);
                }
                .icon { font-size:64px; margin-bottom:20px; display:block; }
                .title {
                  font-size:22px; font-weight:700;
                  color:#ffffff; margin-bottom:12px;
                }
                .domain {
                  font-size:14px; color:#888;
                  margin-bottom:20px; direction:ltr;
                }
                .message { font-size:15px; color:#aaa; line-height:1.7; }
                .divider {
                  width:40px; height:2px;
                  background:linear-gradient(90deg,#444,transparent);
                  margin:24px auto; border-radius:2px;
                }
              </style>
            </head>
            <body>
              <div class="card">
                <span class="icon">$icon</span>
                <div class="title">$title</div>
                <div class="domain">$domain</div>
                <div class="divider"></div>
                <div class="message">$message</div>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    // ── مساعدة ────────────────────────────────────────────────────────────────

    private fun extractDomain(url: String): String? {
        if (url.startsWith("data:") || url.startsWith("about:")) return null
        return try {
            Uri.parse(url).host?.removePrefix("www.")
        } catch (e: Exception) {
            null
        }
    }
}
