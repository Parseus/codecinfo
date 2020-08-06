package com.parseus.codecinfo

import android.annotation.SuppressLint
import android.os.Build
import com.parseus.codecinfo.settings.DarkTheme

@SuppressLint("PrivateApi")
fun getSystemProperty(property: String): String {
    val c = Class.forName("android.os.SystemProperties")
    val get = c.getMethod("get", String::class.java)
    return get.invoke(c, property) as String
}

val isMiUi: Boolean
    get() = getSystemProperty("ro.miui.ui.version.name").isNotEmpty()

fun isBatterySaverDisallowed(): Boolean {
    return Build.VERSION.SDK_INT > 28
            || ("lge".equals(Build.MANUFACTURER, true) && !Build.MODEL.contains("nexus", true))
            || isMiUi
}

fun getDefaultThemeOption() = when {
    Build.VERSION.SDK_INT >= 28 -> DarkTheme.SystemDefault.value
    !isBatterySaverDisallowed() -> DarkTheme.BatterySaver.value
    else -> DarkTheme.Light.value
}