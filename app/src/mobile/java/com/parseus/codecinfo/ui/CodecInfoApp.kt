package com.parseus.codecinfo.ui

import android.app.Application
import androidx.preference.PreferenceManager
import com.kieronquinn.monetcompat.core.MonetCompat
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CodecInfoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (!isNativeMonetAvailable()) {
            MonetCompat.enablePaletteCompat()
            MonetCompat.useSystemColorsOnAndroid12 = isNativeMonetAvailable()
            MonetCompat.wallpaperColorPicker = {
                val userPickedColor = getWallpaperColorFromPreferences()
                it?.firstOrNull { color -> color == userPickedColor } ?: it?.firstOrNull()
            }
        }
    }

    private suspend fun getWallpaperColorFromPreferences(): Int? = withContext(Dispatchers.IO) {
        val color = PreferenceManager.getDefaultSharedPreferences(this@CodecInfoApp)
            .getInt("selected_color", Int.MAX_VALUE)
        return@withContext  if (color == Int.MAX_VALUE) null else color
    }

}