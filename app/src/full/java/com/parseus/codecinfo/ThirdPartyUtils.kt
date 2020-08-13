package com.parseus.codecinfo

import android.content.Context
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mikhaellopez.ratebottomsheet.RateBottomSheet
import com.mikhaellopez.ratebottomsheet.RateBottomSheetManager
import com.samsung.android.sdk.SsdkVendorCheck
import com.samsung.android.sdk.gesture.Sgesture
import com.samsung.android.sdk.gesture.SgestureHand

private var gestureHand: SgestureHand? = null

fun initializeAppRating(activity: AppCompatActivity) {
    RateBottomSheetManager(activity).monitor()
    RateBottomSheet.showRateBottomSheetIfMeetsConditions(activity)
}

fun initializeSamsungGesture(context: Context, pager: ViewPager, tabLayout: TabLayout) {
    if (SsdkVendorCheck.isSamsungDevice()) {
        try {
            val gesture = Sgesture()
            gesture.initialize(context)

            if (gesture.isFeatureEnabled(Sgesture.TYPE_HAND_PRIMITIVE)) {
                gestureHand = SgestureHand(Looper.getMainLooper(), gesture)
                gestureHand!!.start(Sgesture.TYPE_HAND_PRIMITIVE) { info ->
                    if (info.angle in 225..315) {        // to the left
                        tabLayout.setScrollPosition(0, 0f, true)
                        pager.currentItem = 0
                    } else if (info.angle in 45..135) {  // to the right
                        tabLayout.setScrollPosition(1, 0f, true)
                        pager.currentItem = 1
                    }
                }
            }
        } catch (e: Exception) {}
    }
}

fun destroySamsungGestures() {
    gestureHand?.stop()
}