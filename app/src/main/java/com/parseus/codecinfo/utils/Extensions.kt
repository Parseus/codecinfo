package com.parseus.codecinfo.utils

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Typeface
import android.media.MediaCodecInfo
import android.os.BatteryManager
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.annotation.AttrRes
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.drm.DrmSimpleInfo
import java.util.Locale

val externalAppIntentFlags: Int
    get() {
        val flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        return flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
    }

const val AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv"
private const val GOOGLE_ANDROID_TV_INSTALLED = "com.google.android.tv.installed"

private val WHITESPACE_REGEX = Regex("\\s+")

// Workaround for a memory leak from https://issuetracker.google.com/issues/139738913
fun Activity.getMemoryLeakFixBackDispatcher() = object : OnBackPressedCallback(false) {
    override fun handleOnBackPressed() {
        finishAfterTransition()
    }
}

fun FragmentActivity.canEnableMemoryLeakFixBackDispatcher() = Build.VERSION.SDK_INT == 29
        && isTaskRoot
        && (supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.backStackEntryCount ?: 0) == 0
        && supportFragmentManager.backStackEntryCount == 0

private var isTvResult: Boolean? = null

@Suppress("DEPRECATION")
fun Context.isTv(): Boolean {
    isTvResult?.let { return it }

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

    isTv = isTv or packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    isTvResult = isTv
    return isTv
}
fun Context.getAttributeResourceId(@AttrRes attrResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return typedValue.resourceId
}

fun Context.getAttributeDimension(@AttrRes attrResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
}

fun Context.getAttributeColor(@AttrRes attrColor: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrColor, typedValue, true)
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
    return "0x${this.toString(16).uppercase(Locale.getDefault())}"
}

fun ByteArray.toHexString(): String = this.toHexString(HexFormat.Default)

fun String.containsAny(vararg keywords: String): Boolean {
    return keywords.any { this.contains(it, ignoreCase = true) }
}

fun MediaCodecInfo.isAudioCodec(): Boolean {
    return supportedTypes.any { it.contains("audio", true) }
}

fun CodecSimpleInfo.matches(queryWords: List<String>): Boolean {
    return queryWords.all { word ->
        codecId.contains(word, true) || codecName.contains(word, true)
    }
}

fun DrmSimpleInfo.matches(queryWords: List<String>): Boolean {
    return queryWords.all { word ->
        drmName.contains(word, true) || drmUuid.toString().contains(word, true)
    }
}

fun Context.getActivity(): Activity? = this as? Activity
    ?: (this as? ContextWrapper)?.baseContext?.getActivity()

fun getHighlightedText(fullText: String, query: String, highlightColor: Int): CharSequence {
    if (query.isBlank()) {
        return fullText
    }

    val words = query.trim().split(WHITESPACE_REGEX).filter { it.isNotEmpty() }
    if (words.isEmpty()) {
        return fullText
    }

    val spannable = SpannableStringBuilder(fullText)
    val matches = mutableListOf<IntRange>()

    for (word in words) {
        var start = fullText.indexOf(word, ignoreCase = true)
        while (start != -1) {
            matches.add(start until (start + word.length))
            start = fullText.indexOf(word, start + 1, ignoreCase = true)
        }
    }

    // Sort and merge overlapping matches
    if (matches.isNotEmpty()) {
        matches.sortBy { it.first }
        val mergedMatches = mutableListOf<IntRange>()
        var current = matches[0]
        for (i in 1 until matches.size) {
            val next = matches[i]
            if (next.first <= current.last + 1) {
                current = current.first..maxOf(current.last, next.last)
            } else {
                mergedMatches.add(current)
                current = next
            }
        }
        mergedMatches.add(current)

        for (range in mergedMatches) {
            val end = minOf(range.last + 1, fullText.length)
            if (range.first < end) {
                spannable.setSpan(ForegroundColorSpan(highlightColor), range.first, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(StyleSpan(Typeface.BOLD), range.first, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    return spannable
}
