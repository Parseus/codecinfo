package com.parseus.codecinfo.ui.tv

import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.parseus.codecinfo.R

class TvSearchActivity : FragmentActivity(R.layout.tv_search_layout) {

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == 29 && isTaskRoot && supportFragmentManager.backStackEntryCount == 0) {
            // Workaround for a memory leak from https://issuetracker.google.com/issues/139738913
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

}