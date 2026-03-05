package com.parseus.codecinfo.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class SettingsContract : ActivityResultContract<Unit?, Boolean>() {

    override fun createIntent(context: Context, input: Unit?) = Intent(context, TvSettingsActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return if (resultCode != Activity.RESULT_OK) {
            false
        } else {
            intent?.let {
                it.getBooleanExtra(TvSettingsActivity.FILTER_TYPE_CHANGED, false)
                        || it.getBooleanExtra(TvSettingsActivity.SORTING_CHANGED, false)
                        || it.getBooleanExtra(TvSettingsActivity.ALIASES_CHANGED, false)
                        || it.getBooleanExtra(TvSettingsActivity.HW_ICON_CHANGED, false)
                        || it.getBooleanExtra(TvSettingsActivity.SAVE_DETAILS_TO_LOGCAT_CHANGED, false)
                        || it.getBooleanExtra(TvSettingsActivity.HW_ONLY_CODECS_CHANGED, false)
            } ?: false

        }
    }

}