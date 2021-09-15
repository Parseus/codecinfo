package com.parseus.codecinfo.utils

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.media.MediaCodecInfo
import java.util.*

fun Context.isTv(): Boolean {
    val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
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