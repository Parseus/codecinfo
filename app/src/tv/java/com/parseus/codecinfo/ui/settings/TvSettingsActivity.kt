package com.parseus.codecinfo.ui.settings

import android.content.Intent
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

    override fun finish() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ALIASES_CHANGED, aliasesChanged)
            putExtra(FILTER_TYPE_CHANGED, filterTypeChanged)
            putExtra(SORTING_CHANGED, sortingChanged)
            putExtra(HW_ICON_CHANGED, hwIconChanged)
            putExtra(SAVE_DETAILS_TO_LOGCAT_CHANGED, saveDetailsToLogcatChanged)
            putExtra(HW_ONLY_CODECS_CHANGED, hwOnlyCodecsChanged)
        })
        super.finish()

        aliasesChanged = false
        filterTypeChanged = false
        sortingChanged = false
        hwIconChanged = false
        saveDetailsToLogcatChanged = false
        hwOnlyCodecsChanged = false
    }

    companion object {
        var aliasesChanged = false
        var filterTypeChanged = false
        var sortingChanged = false
        var hwIconChanged = false
        var saveDetailsToLogcatChanged = false
        var hwOnlyCodecsChanged = false
        const val ALIASES_CHANGED = "aliases_changed"
        const val FILTER_TYPE_CHANGED = "filter_type_changed"
        const val SORTING_CHANGED = "sorting_changed"
        const val HW_ICON_CHANGED = "hw_icon_changed"
        const val SAVE_DETAILS_TO_LOGCAT_CHANGED = "save_details_to_logcat_changed"
        const val HW_ONLY_CODECS_CHANGED = "hw_only_codecs_changed"
    }

}