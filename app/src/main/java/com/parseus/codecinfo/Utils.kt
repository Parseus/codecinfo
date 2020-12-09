package com.parseus.codecinfo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.parseus.codecinfo.settings.DarkTheme

@SuppressLint("PrivateApi")
fun getSystemProperty(property: String): String? {
    return try {
        val c = Class.forName("android.os.SystemProperties")
        val get = c.getMethod("get", String::class.java)
        get.invoke(c, property) as? String
    } catch (e: Exception) {
        null
    }
}

val isMiUi: Boolean
    get() = !getSystemProperty("ro.miui.ui.version.name").isNullOrEmpty()

val isLgUx: Boolean
    get() = !getSystemProperty("ro.lge.lguiversion").isNullOrEmpty()

val isEmUi: Boolean
    get() = !getSystemProperty("ro.build.version.emui").isNullOrEmpty()

fun isChromebook(context: Context) = context.packageManager.hasSystemFeature("org.chromium.arc")

fun isBatterySaverDisallowed(context: Context): Boolean {
    return Build.VERSION.SDK_INT !in 21..28
            || isLgUx
            || isMiUi
            || isChromebook(context)
}

fun getDefaultThemeOption(context: Context) = when {
    Build.VERSION.SDK_INT >= 28 -> DarkTheme.SystemDefault.value
    !isBatterySaverDisallowed(context) -> DarkTheme.BatterySaver.value
    else -> DarkTheme.Light.value
}