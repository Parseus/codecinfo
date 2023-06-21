package com.parseus.codecinfo.utils

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaCodecInfo
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.getSystemService
import java.util.*

private const val AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv"
private const val GOOGLE_ANDROID_TV_INSTALLED = "com.google.android.tv.installed"

@Suppress("DEPRECATION")
fun Context.isTv(): Boolean {
    // https://developer.android.com/training/tv/start/hardware.html#runtime-check
    var isTv = getSystemService<UiModeManager>()!!.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
            || packageManager.hasSystemFeature(AMAZON_FEATURE_FIRE_TV)
            || packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
            || packageManager.hasSystemFeature(GOOGLE_ANDROID_TV_INSTALLED)

    // https://stackoverflow.com/a/58932366
    if (Build.VERSION.SDK_INT >= 24) {
        val isBatteryAbsent = getSystemService<BatteryManager>()!!
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) == 0
        isTv = isTv or (isBatteryAbsent
                && !packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_ETHERNET))
    }

    if (Build.VERSION.SDK_INT >= 21) {
        isTv = isTv or packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }

    return isTv
}

fun Int.toKiloHertz(): Float {
    return this / 1000f
}

fun Int.toBytesPerSecond(): String {
    return when {
        this == Int.MAX_VALUE -> "2 Gbps"
        this >= 1000000000 -> (this / 1000000000).toString() + " Gbps"
        this >= 1000000 -> (this / 1000000).toString() + " Mbps"
        this >= 1000 -> (this / 1000).toString() + " Kbps"
        else -> "$this bps"
    }
}

fun Int.toHexHstring(): String {
    return "0x${this.toString(16).uppercase(Locale.getDefault())}"
}

fun ByteArray.toHexString(): String {
    return this.joinToString("") { String.format("%02x", it) }
}

fun MediaCodecInfo.isAudioCodec(): Boolean {
    return supportedTypes.joinToString().contains("audio")
}

tailrec fun Context.getActivity(): Activity? = this as? Activity
    ?: (this as? ContextWrapper)?.baseContext?.getActivity()