package com.parseus.codecinfo.utils

import android.util.TypedValue
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.widget.TextViewCompat

object TextSizeCache {
    private val cache = mutableMapOf<String, Float>()

    fun get(text: String, width: Int): Float? = cache["$text|$width"]
    fun put(text: String, width: Int, size: Float) {
        cache["$text|$width"] = size
    }
    fun clear() = cache.clear()
}

fun TextView.setTextWithOptionalAutosizing(
    text: CharSequence,
    minSize: Int,
    maxSize: Int,
    step: Int,
    unit: Int = TypedValue.COMPLEX_UNIT_SP
) {
    val width = width
    if (width > 0) {
        val cachedSize = TextSizeCache.get(text.toString(), width)
        if (cachedSize != null) {
            // Disable auto-sizing and apply cached size immediately
            if (TextViewCompat.getAutoSizeTextType(this) != TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE) {
                TextViewCompat.setAutoSizeTextTypeWithDefaults(this,
                    TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
            }
            setTextSize(TypedValue.COMPLEX_UNIT_PX, cachedSize)
            this.text = text
            return
        }
    }

    // If no cache, ensure auto-sizing is enabled
    if (TextViewCompat.getAutoSizeTextType(this) != TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM) {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            this,
            minSize,
            maxSize,
            step,
            unit
        )
    }
    this.text = text

    if (width > 0) {
        doOnLayout {
            TextSizeCache.put(text.toString(), width, textSize)
        }
    }
}