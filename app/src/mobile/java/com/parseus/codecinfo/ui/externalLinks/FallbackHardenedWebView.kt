package com.parseus.codecinfo.ui.externalLinks

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.parseus.codecinfo.utils.ExternalLinksHelper
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("SetJavaScriptEnabled")
class FallbackHardenedWebView : HardenedWebView {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    var allowedHosts = arrayListOf<String>()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        settings.run {
            javaScriptEnabled = true

            builtInZoomControls = true
            displayZoomControls = true
        }
        webViewClient = HardenedUrlLoadingWebClient()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            allowedHosts = state.allowedHosts
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()?.let {
            SavedState(it).also { state -> state.allowedHosts = allowedHosts }
        }
    }

    private inner class HardenedUrlLoadingWebClient : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            if ("GET" != request.method) {
                return null
            }

            if (request.url.host !in allowedHosts) {
                Log.w("HardenedWebView", "Failed request: ${request.url}")
                return null
            }

            if (getMimeType(request.url.toString()) != null) {
                return super.shouldInterceptRequest(view, request)
            }

            val url = URL(request.url.toString())

            return try {
                val urlConnection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Content-Security-Policy", ExternalLinksHelper.HARDENED_CONTENT_SECURITY_POLICY)
                    setRequestProperty("Permissions-Policy", ExternalLinksHelper.HARDENED_FEATURE_POLICY)
                    setRequestProperty("X-Content-Type-Options", "nosniff")
                }
                WebResourceResponse("text/html", "utf-8", urlConnection.getInputStream())
            } catch (_: Exception) {
                super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            when (extension) {
                "js" -> {
                    return "text/javascript"
                }
                "woff" -> {
                    return "application/font-woff"
                }
                "woff2" -> {
                    return "application/font-woff2"
                }
                "ttf" -> {
                    return "application/x-font-ttf"
                }
                "eot" -> {
                    return "application/vnd.ms-fontobject"
                }
                "svg" -> {
                    return "image/svg+xml"
                }
                else -> type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
        }
        return type
    }

    private class SavedState : BaseSavedState {
        var allowedHosts = arrayListOf<String>()

        constructor(source: Parcel) : super(source) {
            allowedHosts = source.createStringArrayList() ?: arrayListOf()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeStringList(allowedHosts)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

}