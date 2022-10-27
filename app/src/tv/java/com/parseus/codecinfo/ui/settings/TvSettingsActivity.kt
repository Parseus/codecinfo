package com.parseus.codecinfo.ui.settings

import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity

class TvSettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(android.R.id.content, TvSettingsFragment()).commit()
        }

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