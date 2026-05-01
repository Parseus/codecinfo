package com.parseus.codecinfo.utils

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.mikhaellopez.ratebottomsheet.RateBottomSheet
import com.mikhaellopez.ratebottomsheet.RateBottomSheetManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.VARIANT_LIBRARIES
import com.parseus.codecinfo.ui.LicenseDialogManager

const val SHOW_RATE_APP = true

private const val MAX_FLEXIBLE_UPDATE_PRIORITY = 3
private const val MIN_IMMEDIATE_UPDATE_PRIORITY = 4

private var appUpdateManager: AppUpdateManager? = null
private var updateListener: InstallStateUpdatedListener? = null
private var inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

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
            if (installSourcePackage == InstallSource.PlayStore.installerPackageName) {
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

fun checkForUpdate(activity: Activity, progressBar: LinearProgressIndicator?) {
    if (getInstallSourceFromPackageManager(activity) != InstallSource.PlayStore) return

    appUpdateManager = AppUpdateManagerFactory.create(activity)
    appUpdateManager?.appUpdateInfo?.addOnSuccessListener { info ->
        if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            if (info.updatePriority() >= MIN_IMMEDIATE_UPDATE_PRIORITY
                && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateType = UpdateType.Immediate
                inAppUpdateResultLauncher?.let {
                    appUpdateManager?.startUpdateFlowForResult(info, it,
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE))
                }
            } else if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                && info.updatePriority() <= MAX_FLEXIBLE_UPDATE_PRIORITY) {
                appUpdateType = UpdateType.Flexible
                updateListener = InstallStateUpdatedListener { state ->
                    when {
                        state.installStatus() == InstallStatus.DOWNLOADING -> {
                            progressBar!!.isVisible = true
                            val bytesDownloaded = state.bytesDownloaded()
                            val totalBytesToDownload = state.totalBytesToDownload().takeIf { it > 0 } ?: bytesDownloaded
                            val currentProgress = (bytesDownloaded * 100 / totalBytesToDownload).toInt()
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
                updateListener?.let { appUpdateManager?.registerListener(it) }
                inAppUpdateResultLauncher?.let {
                    appUpdateManager?.startUpdateFlowForResult(info, it,
                        AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE))
                }
            }
        }
    }
}

fun handleAppUpdateOnActivityResult(activity: Activity, resultCode: Int) {
    if (resultCode == Activity.RESULT_CANCELED) {
        updateListener?.let { appUpdateManager?.unregisterListener(it) }
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
    appUpdateManager?.appUpdateInfo?.addOnSuccessListener { info ->
        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            updateListener?.let { appUpdateManager?.unregisterListener(it) }
            showSnackbarForDownloadedUpdate(activity)
        }
    }
}

private fun handleImmediateUpdateOnResume(activity: Activity) {
    appUpdateManager?.appUpdateInfo?.addOnSuccessListener { info ->
        if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            appUpdateManager?.startUpdateFlow(info, activity, AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE))
        }
    }
}

private fun showSnackbarForDownloadedUpdate(activity: Activity) {
    Snackbar.make(activity.findViewById(android.R.id.content),
        R.string.update_flexible_complete, Snackbar.LENGTH_INDEFINITE).apply {
        setAction(R.string.update_flexible_restart) { appUpdateManager?.completeUpdate() }
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
            } catch (_: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, source.getWebUri(packageName))
                startActivity(webIntent)
            }
        }
    }
}

fun showLicensesDialog(activity: AppCompatActivity) {
    val manager = LicenseDialogManager(activity)
    VARIANT_LIBRARIES.forEach { manager.setLibrary(it) }
    manager.show()
}

fun cleanInAppUpdateReferences() {
    updateListener?.let { appUpdateManager?.unregisterListener(it) }
    appUpdateManager = null
    updateListener = null
    inAppUpdateResultLauncher = null
}
