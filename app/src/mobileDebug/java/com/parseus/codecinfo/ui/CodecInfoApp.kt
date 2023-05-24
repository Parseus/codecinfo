package com.parseus.codecinfo.ui

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.core.WallpaperTypes
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CodecInfoApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (isNativeMonetAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this,
                DynamicColorsOptions.Builder().setPrecondition { _, _ -> isDynamicThemingEnabled(this) }.build())
        } else if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT <= 26) {
                MonetCompat.enablePaletteCompat()
            }
            MonetCompat.useSystemColorsOnAndroid12 = false
            MonetCompat.wallpaperSource = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("dynamic_theme_wallpaper_source", WallpaperTypes.WALLPAPER_SYSTEM.toString())!!.toInt()
            MonetCompat.wallpaperColorPicker = {
                val userPickedColor = getWallpaperColorFromPreferences()
                it?.firstOrNull { color -> color == userPickedColor } ?: it?.firstOrNull()
            }
        }

        enableSettingsIntentFilter()
    }

    private suspend fun getWallpaperColorFromPreferences(): Int? = withContext(Dispatchers.IO) {
        val color = PreferenceManager.getDefaultSharedPreferences(this@CodecInfoApp)
            .getInt("selected_color", Int.MAX_VALUE)
        return@withContext  if (color == Int.MAX_VALUE) null else color
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