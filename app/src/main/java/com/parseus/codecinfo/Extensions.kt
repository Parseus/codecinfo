package com.parseus.codecinfo

import android.media.MediaCodecInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachRoot)
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
        else -> this.toString() + " bps"
    }
}

fun Int.toHexHstring(): String {
    return "0x${this.toString(16).toUpperCase()}"
}

fun MediaCodecInfo.isAudioCodec(): Boolean {
    return supportedTypes.joinToString().contains("audio")
}