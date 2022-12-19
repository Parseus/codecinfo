package com.parseus.codecinfo.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.parseus.codecinfo.utils.isNightMode

@SuppressLint("NewApi")
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class ChangelogWebView : WebView {

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

            val isNightMode = context.isNightMode()
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isNightMode)
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
        CookieManager.getInstance().setAcceptCookie(false)
        webViewClient = HardenedAssetLoadingWebClient()
        loadUrl("https://localhost/changelog.html")
    }

    inner class HardenedAssetLoadingWebClient : WebViewClient() {

        private fun interceptRequest(url: Uri): WebResourceResponse? {
            if ("localhost" != url.host) {
                return null
            }

            if ("/changelog.html" == url.path) {
                return try {
                    val inputStream = context.assets.open(url.path!!.substring(1))
                    WebResourceResponse("text/html", null, inputStream).also {
                        if (Build.VERSION.SDK_INT >= 21) {
                            it.responseHeaders = mapOf(
                                "Content-Security-Policy" to CONTENT_SECURITY_POLICY,
                                "Permissions-Policy" to PERMISSIONS_POLICY,
                                "X-Content-Type-Options" to "nosniff"
                            )
                        }
                    }
                } catch (e: Exception) { null }
            }

            return null
        }

        @RequiresApi(21)
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            if ("GET" != request.method) {
                return null
            }

            return interceptRequest(request.url)
        }

        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            return interceptRequest(Uri.parse(url))
        }

        @RequiresApi(24) override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
        override fun shouldOverrideUrlLoading(view: WebView, url: String) = true

    }

    companion object {
        private const val CONTENT_SECURITY_POLICY =
            "default-src 'none'; " +
            "form-action 'none'; " +
            "connect-src https://localhost/changelog.html; " +
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