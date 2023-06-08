package com.parseus.codecinfo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
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

private const val MAX_FLEXIBLE_UPDATE_PRIORITY = 3
private const val MIN_IMMEDIATE_UPDATE_PRIORITY = 4

private lateinit var appUpdateManager: AppUpdateManager
private lateinit var updateListener: InstallStateUpdatedListener
private lateinit var inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>

enum class UpdateType {
    Flexible, Immediate, Unknown
}
private var appUpdateType = UpdateType.Unknown

fun createInAppUpdateResultLauncher(activity: AppCompatActivity) {
    inAppUpdateResultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        handleAppUpdateOnActivityResult(activity, it.resultCode)
    }
}

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
                    if (it.isSuccessful && it.result != null) {
                        val flow = manager.launchReviewFlow(activity, it.result!!)
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
        } catch (_: Exception) {}
    }
}

fun destroySamsungGestures() {
    gestureHand?.stop()
}

fun checkForUpdate(activity: Activity, progressBar: LinearProgressIndicator?) {
    if (Build.VERSION.SDK_INT < 21 || getInstallSourceFromPackageManager(activity) != InstallSource.PlayStore) return

    appUpdateManager = AppUpdateManagerFactory.create(activity)
    appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
        if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            if (info.updatePriority() >= MIN_IMMEDIATE_UPDATE_PRIORITY
                && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateType = UpdateType.Immediate
                appUpdateManager.startUpdateFlowForResult(info, inAppUpdateResultLauncher,
                    AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE))
            } else if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                && info.updatePriority() <= MAX_FLEXIBLE_UPDATE_PRIORITY) {
                appUpdateType = UpdateType.Flexible
                updateListener = InstallStateUpdatedListener { state ->
                    when {
                        state.installStatus() == InstallStatus.DOWNLOADING -> {
                            progressBar!!.isVisible = true
                            val bytesDownloaded = state.bytesDownloaded()
                            val totalBytesToDownload = state.totalBytesToDownload()
                            val currentProgress = (bytesDownloaded / totalBytesToDownload).toInt() * 100
                            progressBar.setProgressCompat(currentProgress, true)
                            progressBar.contentDescription = activity.getString(R.string.update_flexible_progress_description, currentProgress)
                        }
                        state.installStatus() in InstallStatus.FAILED..InstallStatus.CANCELED -> {
                            progressBar!!.isVisible = false
                        }
                        state.installStatus() == InstallStatus.DOWNLOADED -> {
                            showSnackbarForDownloadedUpdate(activity)
                        }
                    }
                }
                appUpdateManager.registerListener(updateListener)
                appUpdateManager.startUpdateFlowForResult(info, inAppUpdateResultLauncher,
                    AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE))
            }
        }
    }
}

fun handleAppUpdateOnActivityResult(activity: Activity, resultCode: Int) {
    if (resultCode == Activity.RESULT_CANCELED) {
        appUpdateManager.unregisterListener(updateListener)
    } else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
        Snackbar.make(activity.findViewById(android.R.id.content),
            R.string.update_failed, Snackbar.LENGTH_LONG).show()
    }
}

fun handleAppUpdateOnResume(activity: Activity) {
    if (appUpdateType == UpdateType.Flexible) {
        handleFlexibleUpdateOnResume(activity)
    } else if (appUpdateType == UpdateType.Immediate) {
        handleImmediateUpdateOnResume(activity)
    }
}

private fun handleFlexibleUpdateOnResume(activity: Activity) {
    appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager.unregisterListener(updateListener)
            showSnackbarForDownloadedUpdate(activity)
        }
    }
}

private fun handleImmediateUpdateOnResume(activity: Activity) {
    appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
        if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            appUpdateManager.startUpdateFlow(info, activity, AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE))
        }
    }
}

private fun showSnackbarForDownloadedUpdate(activity: Activity) {
    Snackbar.make(activity.findViewById(android.R.id.content),
        R.string.update_flexible_complete, Snackbar.LENGTH_INDEFINITE).apply {
        setAction(R.string.update_flexible_restart) { appUpdateManager.completeUpdate() }
        show()
    }
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

fun showLicensesDialog(activity: AppCompatActivity) {
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
        .setLibrary(Library("MonetCompat", "https://github.com/KieronQuinn/MonetCompat", License.MIT))
        .setPositiveButton(android.R.string.ok, null)
        .setBackgroundColor(getSurfaceColor(activity))
        .show()
}