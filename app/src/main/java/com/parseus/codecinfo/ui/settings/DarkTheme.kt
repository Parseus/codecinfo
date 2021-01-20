package com.parseus.codecinfo.ui.settings

import androidx.appcompat.app.AppCompatDelegate

enum class DarkTheme(val value: Int) {

    Light(0),
    Dark(1),
    BatterySaver(2),
    SystemDefault(3);

    companion object {
        fun getAppCompatValue(enumValue: Int): Int {
            return when (enumValue) {
                Light.value -> AppCompatDelegate.MODE_NIGHT_NO
                Dark.value -> AppCompatDelegate.MODE_NIGHT_YES
                BatterySaver.value -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
    }

}