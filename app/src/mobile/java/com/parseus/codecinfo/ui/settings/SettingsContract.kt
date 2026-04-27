package com.parseus.codecinfo.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

data class SettingsResult(
    val filterTypeChanged: Boolean = false,
    val sortingChanged: Boolean = false,
    val immersiveChanged: Boolean = false,
    val aliasesChanged: Boolean = false,
    val dynamicThemeChanged: Boolean = false,
    val hwIconChanged: Boolean = false,
    val saveDetailsToLogcatChanged: Boolean = false,
    val hwOnlyCodecsChanged: Boolean = false
) {
    fun shouldReloadLists() = filterTypeChanged || sortingChanged || aliasesChanged
            || hwIconChanged || hwOnlyCodecsChanged
}

class SettingsContract : ActivityResultContract<Unit?, SettingsResult>() {

    override fun createIntent(context: Context, input: Unit?) = Intent(context, SettingsActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): SettingsResult {
        return if (resultCode != Activity.RESULT_OK || intent == null) {
            SettingsResult()
        } else {
            SettingsResult(
                filterTypeChanged = intent.getBooleanExtra(SettingsActivity.FILTER_TYPE_CHANGED, false),
                sortingChanged = intent.getBooleanExtra(SettingsActivity.SORTING_CHANGED, false),
                immersiveChanged = intent.getBooleanExtra(SettingsActivity.IMMERSIVE_CHANGED, false),
                aliasesChanged = intent.getBooleanExtra(SettingsActivity.ALIASES_CHANGED, false),
                dynamicThemeChanged = intent.getBooleanExtra(SettingsActivity.DYNAMIC_THEME_CHANGED, false),
                hwIconChanged = intent.getBooleanExtra(SettingsActivity.HW_ICON_CHANGED, false),
                saveDetailsToLogcatChanged = intent.getBooleanExtra(SettingsActivity.SAVE_DETAILS_TO_LOGCAT_CHANGED, false),
                hwOnlyCodecsChanged = intent.getBooleanExtra(SettingsActivity.HW_ONLY_CODECS_CHANGED, false)
            )
        }
    }

}
