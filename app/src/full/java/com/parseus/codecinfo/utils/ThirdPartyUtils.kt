package com.parseus.codecinfo.utils

import android.content.Context
import android.os.Build
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewManagerFactory
import com.mikhaellopez.ratebottomsheet.InstallSource
import com.mikhaellopez.ratebottomsheet.RateBottomSheet
import com.mikhaellopez.ratebottomsheet.RateBottomSheetManager
import com.samsung.android.sdk.SsdkVendorCheck
import com.samsung.android.sdk.gesture.Sgesture
import com.samsung.android.sdk.gesture.SgestureHand

private var gestureHand: SgestureHand? = null

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