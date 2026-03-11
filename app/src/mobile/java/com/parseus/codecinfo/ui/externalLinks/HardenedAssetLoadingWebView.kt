package com.parseus.codecinfo.ui.externalLinks

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.parseus.codecinfo.utils.ExternalLinksHelper

@Suppress("OVERRIDE_DEPRECATION")
class HardenedAssetLoadingWebView : HardenedWebView {

    lateinit var openExternalLink: (Uri) -> Unit

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        settings.blockNetworkLoads = true
        webViewClient = HardenedAssetLoadingWebClient()
    }

    private inner class HardenedAssetLoadingWebClient : WebViewClient() {

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
                            "Content-Security-Policy" to ExternalLinksHelper.HARDENED_CONTENT_SECURITY_POLICY,
                            "Permissions-Policy" to ExternalLinksHelper.HARDENED_FEATURE_POLICY,
                            "X-Content-Type-Options" to "nosniff"
                        )
                    }
                } catch (_: Exception) { null }
            }

            return null
        }

        @RequiresApi(24) override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if ("https" == request.url.scheme) {
                openExternalLink.invoke(request.url)
            }
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("https://")) {
                openExternalLink.invoke(url.toUri())
            }
            return true
        }

    }

}