package com.parseus.codecinfo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.parseus.codecinfo.settings.SettingsActivity

class SettingsContract : ActivityResultContract<Unit, Boolean>() {

    override fun createIntent(context: Context, input: Unit) = Intent(context, SettingsActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return if (resultCode != Activity.RESULT_OK) {
            false
        } else {
            intent?.let {
                it.getBooleanExtra(SettingsActivity.FILTER_TYPE_CHANGED, false)
                        || it.getBooleanExtra(SettingsActivity.SORTING_CHANGED, false)
                        || it.getBooleanExtra(SettingsActivity.IMMERSIVE_CHANGED, false)
                        || it.getBooleanExtra(SettingsActivity.ALIASES_CHANGED, false)
            } ?: false

        }
    }

}