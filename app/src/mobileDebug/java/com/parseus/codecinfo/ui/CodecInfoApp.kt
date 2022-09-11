package com.parseus.codecinfo.ui

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

        if (!isNativeMonetAvailable()) {
            MonetCompat.enablePaletteCompat()
            MonetCompat.useSystemColorsOnAndroid12 = isNativeMonetAvailable()
            MonetCompat.wallpaperSource = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("dynamic_theme_wallpaper_source", WallpaperTypes.WALLPAPER_SYSTEM.toString())!!.toInt()
            MonetCompat.wallpaperColorPicker = {
                val userPickedColor = getWallpaperColorFromPreferences()
                it?.firstOrNull { color -> color == userPickedColor } ?: it?.firstOrNull()
            }
        } else {
            DynamicColors.applyToActivitiesIfAvailable(this,
                DynamicColorsOptions.Builder().setPrecondition { _, _ -> isDynamicThemingEnabled(this) }.build())
        }
    }

    private suspend fun getWallpaperColorFromPreferences(): Int? = withContext(Dispatchers.IO) {
        val color = PreferenceManager.getDefaultSharedPreferences(this@CodecInfoApp)
            .getInt("selected_color", Int.MAX_VALUE)
        return@withContext  if (color == Int.MAX_VALUE) null else color
    }

}