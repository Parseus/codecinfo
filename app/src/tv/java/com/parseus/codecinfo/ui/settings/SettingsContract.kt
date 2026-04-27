package com.parseus.codecinfo.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

data class SettingsResult(
    val filterTypeChanged: Boolean = false,
    val sortingChanged: Boolean = false,
    val aliasesChanged: Boolean = false,
    val hwIconChanged: Boolean = false,
    val saveDetailsToLogcatChanged: Boolean = false,
    val hwOnlyCodecsChanged: Boolean = false
) {
    fun shouldReloadLists() = filterTypeChanged || sortingChanged || aliasesChanged
            || hwIconChanged || hwOnlyCodecsChanged
}

class SettingsContract : ActivityResultContract<Unit?, SettingsResult>() {

    override fun createIntent(context: Context, input: Unit?) = Intent(context, TvSettingsActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): SettingsResult {
        return if (resultCode != Activity.RESULT_OK || intent == null) {
            SettingsResult()
        } else {
            SettingsResult(
                filterTypeChanged = intent.getBooleanExtra(TvSettingsActivity.FILTER_TYPE_CHANGED, false),
                sortingChanged = intent.getBooleanExtra(TvSettingsActivity.SORTING_CHANGED, false),
                aliasesChanged = intent.getBooleanExtra(TvSettingsActivity.ALIASES_CHANGED, false),
                hwIconChanged = intent.getBooleanExtra(TvSettingsActivity.HW_ICON_CHANGED, false),
                saveDetailsToLogcatChanged = intent.getBooleanExtra(TvSettingsActivity.SAVE_DETAILS_TO_LOGCAT_CHANGED, false),
                hwOnlyCodecsChanged = intent.getBooleanExtra(TvSettingsActivity.HW_ONLY_CODECS_CHANGED, false)
            )
        }
    }

}
