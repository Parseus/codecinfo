package com.parseus.codecinfo.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import com.parseus.codecinfo.R

class TvDeviceIssuesActivity : FragmentActivity(R.layout.activity_tv_device_issues) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            if (Build.VERSION.SDK_INT == 29 && isTaskRoot && supportFragmentManager.backStackEntryCount == 0) {
                // Workaround for a memory leak from https://issuetracker.google.com/issues/139738913
                finishAfterTransition()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

}