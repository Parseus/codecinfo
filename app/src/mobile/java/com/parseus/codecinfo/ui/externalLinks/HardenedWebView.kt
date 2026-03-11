package com.parseus.codecinfo.ui.externalLinks

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

@Suppress("DEPRECATION")
open class HardenedWebView : WebView {

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

            loadWithOverviewMode = true
            useWideViewPort = true
        }
        if (Build.VERSION.SDK_INT >= 26) {
            importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
        }
        CookieManager.getInstance().setAcceptCookie(false)
    }

}