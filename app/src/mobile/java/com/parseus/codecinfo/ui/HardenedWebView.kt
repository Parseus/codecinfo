package com.parseus.codecinfo.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.core.net.toUri

@SuppressLint("NewApi")
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class HardenedWebView : WebView {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        settings.apply {
            allowContentAccess = false
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            saveFormData = false
        }
        if (Build.VERSION.SDK_INT >= 26) {
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
        CookieManager.getInstance().setAcceptCookie(false)
        webViewClient = HardenedAssetLoadingWebClient()
    }

    inner class HardenedAssetLoadingWebClient : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            if ("GET" != request.method) {
                return null
            }

            val url = request.url

            if ("localhost" != url.host) {
                return null
            }

            if (url.toString().contains("base64")) {
                return try {
                    val inputStream = url.toString().byteInputStream()
                    WebResourceResponse("text/html", null, inputStream).also {
                        it.responseHeaders = mapOf(
                            "Content-Security-Policy" to CONTENT_SECURITY_POLICY,
                            "Permissions-Policy" to PERMISSIONS_POLICY,
                            "X-Content-Type-Options" to "nosniff"
                        )
                    }
                } catch (e: Exception) { null }
            }

            return null
        }

        @RequiresApi(24) override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if ("https" == request.url.scheme) {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
            }
            return true
        }
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("https://")) {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
            return true
        }

    }

    companion object {
        private const val CONTENT_SECURITY_POLICY =
            "default-src 'none'; " +
                    "form-action 'none'; " +
                    "img-src blob: 'self'; " +
                    "script-src 'self'; " +
                    "style-src 'self'; " +
                    "frame-ancestors 'none'; " +
                    "base-uri 'none'"
        private const val PERMISSIONS_POLICY =
            "accelerometer=(), " +
                    "ambient-light-sensor=(), " +
                    "autoplay=(), " +
                    "battery=(), " +
                    "camera=(), " +
                    "clipboard-read=(), " +
                    "clipboard-write=(), " +
                    "display-capture=(), " +
                    "document-domain=(), " +
                    "encrypted-media=(), " +
                    "fullscreen=(), " +
                    "gamepad=(), " +
                    "geolocation=(), " +
                    "gyroscope=(), " +
                    "hid=(), " +
                    "idle-detection=(), " +
                    "interest-cohort=(), " +
                    "magnetometer=(), " +
                    "microphone=(), " +
                    "midi=(), " +
                    "payment=(), " +
                    "picture-in-picture=(), " +
                    "publickey-credentials-get=(), " +
                    "screen-wake-lock=(), " +
                    "serial=(), " +
                    "speaker-selection=(), " +
                    "sync-xhr=(), " +
                    "usb=(), " +
                    "xr-spatial-tracking=()"
    }

}