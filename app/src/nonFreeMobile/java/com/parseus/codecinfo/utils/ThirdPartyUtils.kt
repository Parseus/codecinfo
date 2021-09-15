package com.parseus.codecinfo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewManagerFactory
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import com.mikhaellopez.ratebottomsheet.RateBottomSheet
import com.mikhaellopez.ratebottomsheet.RateBottomSheetManager
import com.parseus.codecinfo.R
import com.samsung.android.sdk.SsdkVendorCheck
import com.samsung.android.sdk.gesture.Sgesture
import com.samsung.android.sdk.gesture.SgestureHand

private var gestureHand: SgestureHand? = null
const val SHOW_RATE_APP = true

fun initializeAppRating(activity: AppCompatActivity) {
    val rateManager = RateBottomSheetManager(activity)
    rateManager.monitor()

    if (rateManager.shouldShowRateBottomSheet()) {
        activity.run {
            val installSourcePackage = if (Build.VERSION.SDK_INT >= 30) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
            if (installSourcePackage == InstallSource.PlayStore.installerPackageName
                    && Build.VERSION.SDK_INT >= 21) {
                val manager = ReviewManagerFactory.create(activity)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val flow = manager.launchReviewFlow(activity, it.result)
                        flow.addOnCompleteListener { rateManager.disableAgreeShowDialog() }
                    }
                }
            } else {
                RateBottomSheet.showRateBottomSheetIfMeetsConditions(activity,
                        InstallSource.fromInstallSource(installSourcePackage))
            }
        }
    }
}

fun initializeSamsungGesture(context: Context, pager: ViewPager2, tabLayout: TabLayout) {
    if (SsdkVendorCheck.isSamsungDevice()) {
        try {
            val gesture = Sgesture()
            gesture.initialize(context)

            if (gesture.isFeatureEnabled(Sgesture.TYPE_HAND_PRIMITIVE)) {
                gestureHand = SgestureHand(Looper.getMainLooper(), gesture)
                gestureHand!!.start(Sgesture.TYPE_HAND_PRIMITIVE) { info ->
                    if (info.angle in 225..315) {        // to the left
                        val newPosition = (pager.currentItem - 1) and tabLayout.tabCount
                        tabLayout.setScrollPosition(newPosition, 0f, true)
                        pager.currentItem = newPosition
                    } else if (info.angle in 45..135) {  // to the right
                        val newPosition = (pager.currentItem + 1) and tabLayout.tabCount
                        tabLayout.setScrollPosition(newPosition, 0f, true)
                        pager.currentItem = newPosition
                    }
                }
            }
        } catch (e: Exception) {}
    }
}

fun destroySamsungGestures() {
    gestureHand?.stop()
}

private fun getInstallSourceFromPackageManager(activity: Activity): InstallSource? {
    activity.run {
        val installSourcePackage = if (Build.VERSION.SDK_INT >= 30) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName)
        }
        return InstallSource.fromInstallSource(installSourcePackage)
    }
}

fun launchStoreIntent(activity: Activity) {
    activity.run {
        val installSource = getInstallSourceFromPackageManager(this)
        installSource?.let { source ->
            val marketIntent = Intent(Intent.ACTION_VIEW, source.getMarketUri(packageName))
            marketIntent.addFlags(externalAppIntentFlags)
            try {
                startActivity(marketIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, source.getWebUri(packageName))
                startActivity(webIntent)
            }
        }
    }
}

inline fun showLicensesDialog(activity: AppCompatActivity) {
    LicenserDialog(activity)
        .setTitle(R.string.about_licenses)
        .setLibrary(Library("Android Jetpack", "https://developer.android.com/jetpack", License.APACHE2))
        .setLibrary(Library("Kotlin", "https://github.com/JetBrains/kotlin", License.APACHE2))
        .setLibrary(Library("Kotlin Coroutines", "https://github.com/Kotlin/kotlinx.coroutines", License.APACHE2))
        .setLibrary(Library("LeakCanary", "https://github.com/square/leakcanary", License.APACHE2))
        .setLibrary(Library("Material Components for Android", "https://github.com/material-components/material-components-android", License.APACHE2))
        .setLibrary(Library("Moshi", "https://github.com/square/moshi", License.APACHE2))
        .setLibrary(Library("Okio", "https://github.com/square/okio", License.APACHE2))
        .setLibrary(Library("RateBottomSheet", "https://github.com/lopspower/RateBottomSheet", License.APACHE2))
        .setLibrary(Library("Licenser", "https://github.com/marcoscgdev/Licenser", License.MIT))
        .setPositiveButton(android.R.string.ok, null)
        .show()
}