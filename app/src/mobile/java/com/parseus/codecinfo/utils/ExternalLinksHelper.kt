package com.parseus.codecinfo.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.ui.externalLinks.FallbackWebBrowserDialog

class ExternalLinksHelper(private val context: Context, lifecycle: Lifecycle) : DefaultLifecycleObserver {

    private enum class OpenInMethod(val value: Int) {
        CustomTabs(0),
        ExternalBrowser(1),
        WebView(2);

        companion object {
            fun from(value: Int) = entries.first { it.value == value }
        }
    }

    private var customTabsClient: CustomTabsClient? = null
    private var customTabsConnection: CustomTabsServiceConnection? = null
    private var customTabsSession: CustomTabsSession? = null

    private val session: CustomTabsSession?
        get() {
            if (customTabsClient == null) {
                customTabsSession = null
            } else if (customTabsSession == null) {
                customTabsSession = customTabsClient!!.newSession(CustomTabsCallback())
            }
            return customTabsSession
        }

    init {
        lifecycle.addObserver(this)
    }

    @Suppress("DEPRECATION")
    private fun getPackageNameToUse(context: Context): String? {
        val pm = context.packageManager

        val activityIntent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            data = Uri.fromParts("https", "", null)
        }

        val supportedPackages = mutableListOf<String>()
        pm.queryIntentActivities(activityIntent).sortedByDescending { it.preferredOrder }.forEach { info ->
            val serviceIntent = Intent().apply {
                action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
                `package` = info.activityInfo.packageName
            }

            val services = if (Build.VERSION.SDK_INT >= 33) {
                pm.resolveService(serviceIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                pm.resolveService(serviceIntent, 0)
            }
            if (services != null) {
                supportedPackages.add(info.activityInfo.packageName)
            }
        }

        return supportedPackages.firstOrNull()
    }

    override fun onPause(owner: LifecycleOwner) {
        customTabsConnection?.let {
            context.unbindService(it)
            customTabsClient = null
            customTabsConnection = null
            customTabsSession = null
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        if (customTabsClient != null) return

        val packageName = getPackageNameToUse(context) ?: return
        customTabsConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                customTabsClient = client.also { it.warmup(0L) }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                customTabsClient = null
                customTabsSession = null
            }
        }
        CustomTabsClient.bindCustomTabsService(context, packageName, customTabsConnection!!)
    }

    fun launchInBrowser(activity: FragmentActivity, uri: Uri) {
        val nativeAppLaunched = if (Build.VERSION.SDK_INT >= 30) {
            launchNativeApi30(activity, uri)
        } else {
            launchNative(activity, uri)
        }

        if (!nativeAppLaunched) {
            val settings = context.settingsRepository.getSettingsSync()
            var openInMethod = OpenInMethod.from(settings.openExternalLinks.toInt())
            if (openInMethod == OpenInMethod.WebView && uri.toString().contains("developer.android.com")) {
                // Android documentation doesn't load in WebView for some reason, so load it externally.
                openInMethod = OpenInMethod.ExternalBrowser
            }
            when (openInMethod) {
                OpenInMethod.CustomTabs -> {
                    val customTabOpened = launchInCustomTabs(activity, uri)
                    if (!customTabOpened) {
                        showErrorSnackbar(activity)
                    }
                }
                OpenInMethod.ExternalBrowser -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.addFlags(externalAppIntentFlags)
                        activity.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        showErrorSnackbar(activity)
                    }
                }
                OpenInMethod.WebView -> {
                    if (activity.packageManager.hasSystemFeature("android.software.webview")) {
                        FallbackWebBrowserDialog.showDialog(uri, activity.supportFragmentManager)
                    } else {
                        showErrorSnackbar(activity)
                    }
                }
            }
        }
    }

    private fun showErrorSnackbar(activity: Activity) {
        Snackbar.make(activity.findViewById(android.R.id.content),
            activity.getString(R.string.no_apps_for_action), Snackbar.LENGTH_LONG).show()
    }

    private fun launchInCustomTabs(context: Context, uri: Uri): Boolean {
        val customTabsIntent = CustomTabsIntent.Builder(session).build()
        customTabsIntent.intent.putExtra(Browser.EXTRA_HEADERS, Bundle().apply {
            putString("Content-Security-Policy", HARDENED_CONTENT_SECURITY_POLICY)
            putString("Feature-Policy", HARDENED_FEATURE_POLICY)
            putString("X-Content-Type-Options", "nosniff")
        })
        return try {
            customTabsIntent.launchUrl(context, uri)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    @RequiresApi(30)
    private fun launchNativeApi30(context: Context, uri: Uri): Boolean {
        val nativeAppIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
        }

        return try {
            context.startActivity(nativeAppIntent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    private fun launchNative(context: Context, uri: Uri): Boolean {
        val pm = context.packageManager

        // Get all apps that resolve a generic URL.
        val browserActivityIntent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            data = Uri.fromParts("https", "", null)
        }
        val genericResolvedList = pm.queryIntentActivities(browserActivityIntent)
            .map { it.activityInfo.packageName }.toSet()

        // Get all apps that resolve the specific URL.
        val specializedActivityIntent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val specializedResolvedList = pm.queryIntentActivities(specializedActivityIntent)
            .map { it.activityInfo.packageName }.toMutableSet()

        // Keep only the URLs that resolve the specific, but not the generic URLs.
        specializedResolvedList.removeAll(genericResolvedList)

        // If the list is empty, no native app handlers were found.
        if (specializedResolvedList.isEmpty()) {
            return false
        }

        // We found native handlers. Launch the Intent.
        specializedActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(specializedActivityIntent)

        return true
    }

    @Suppress("DEPRECATION")
    private fun PackageManager.queryIntentActivities(intent: Intent) : List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= 33) {
            queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            queryIntentActivities(intent, 0)
        }
    }

    companion object {

        const val HARDENED_CONTENT_SECURITY_POLICY =
            "default-src 'none'; " +
            "form-action 'none'; " +
            "connect-src 'none'; " +
            "img-src blob: 'self'; " +
            "script-src 'self'; " +
            "style-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'none'"
        const val HARDENED_FEATURE_POLICY =
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