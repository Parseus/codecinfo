package com.parseus.codecinfo.utils

import android.content.Context
import android.media.MediaCodecInfo
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.parseus.codecinfo.R
import java.util.*

fun Context.isInTwoPaneMode(): Boolean {
    return resources.getBoolean(R.bool.twoPaneMode)
}

fun Context.getAttributeColor(@AttrRes attrColor: Int,
                              typedValue: TypedValue = TypedValue(),
                              resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
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
    return "0x${this.toString(16).toUpperCase(Locale.getDefault())}"
}

fun ByteArray.toHexString(): String {
    return this.joinToString("") { String.format("%02x", it) }
}

fun MediaCodecInfo.isAudioCodec(): Boolean {
    return supportedTypes.joinToString().contains("audio")
}