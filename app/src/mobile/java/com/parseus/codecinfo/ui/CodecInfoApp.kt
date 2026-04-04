package com.parseus.codecinfo.ui

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.kieronquinn.monetcompat.core.MonetCompat
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CodecInfoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize DataStore and trigger migration if needed
        val settings = runBlocking { settingsRepository.settingsFlow.first() }

        if (isNativeMonetAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this,
                DynamicColorsOptions.Builder().setPrecondition { _, _ -> isDynamicThemingEnabled(this) }.build())
        } else {
            if (Build.VERSION.SDK_INT <= 26) {
                MonetCompat.enablePaletteCompat()
            }
            MonetCompat.useSystemColorsOnAndroid12 = false
            MonetCompat.wallpaperSource = settings.dynamicThemeWallpaperSource.toInt()
            MonetCompat.wallpaperColorPicker = {
                val userPickedColor = settings.selectedColor
                it?.firstOrNull { color -> color == userPickedColor } ?: it?.firstOrNull()
            }
        }

        enableSettingsIntentFilter()
    }

    private fun enableSettingsIntentFilter() {
        val pm = packageManager
        val standardComponentName = ComponentName(packageName, "alias.SettingsActivity")
        val samsungComponentName = ComponentName(packageName, "alias.SettingsActivitySamsung")

        // This is done to avoid duplicate settings entries on Samsung devices.
        if (Build.MANUFACTURER != "samsung") {
            pm.setComponentEnabledSetting(samsungComponentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(standardComponentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        } else {
            pm.setComponentEnabledSetting(samsungComponentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(standardComponentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }

}