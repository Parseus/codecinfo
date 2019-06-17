package com.parseus.codecinfo.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

enum class DarkTheme(val value: Int) {

    Light(0),
    Dark(1),
    SystemDefault(2);

    companion object {
        fun getAppCompatValue(enumValue: Int): Int {
            return when (enumValue) {
                Light.value -> AppCompatDelegate.MODE_NIGHT_NO
                Dark.value -> AppCompatDelegate.MODE_NIGHT_YES
                else -> if (Build.VERSION.SDK_INT >= 29)
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        }
    }

}