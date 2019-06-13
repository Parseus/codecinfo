package com.parseus.codecinfo

import android.content.Context
import android.media.MediaCodecInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import java.util.*

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachRoot)
}

fun Context.isInTwoPaneMode(): Boolean {
    return resources.getBoolean(R.bool.twoPaneMode)
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

fun MediaCodecInfo.isAudioCodec(): Boolean {
    return supportedTypes.joinToString().contains("audio")
}

fun MediaCodecInfo.isAccelerated(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isHardwareAccelerated
    } else {
        (name.contains("OMX.brcm.video", true) && name.contains("hw", true))
                || !(name.startsWith("OMX.google.", true)
                || name.startsWith("c2.android.", true)
                || (!name.startsWith("OMX.", true) && !name.startsWith("c2.", true))
                || name.endsWith("sw", true)
                || name.endsWith("sw.dec", true) || name.endsWith("swvdec", true))
    }
}

fun <T : View> View.bind(@IdRes idRes: Int): Lazy<T> {
    return unsafeLazy { findViewById<T>(idRes) }
}

private fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)