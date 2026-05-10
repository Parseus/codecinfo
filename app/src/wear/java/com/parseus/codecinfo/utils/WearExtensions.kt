package com.parseus.codecinfo.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.*
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.view.ViewConfigurationCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.input.WearableButtons
import com.parseus.codecinfo.R

@Suppress("unused")
fun Context.copyToClipboard(label: String, text: String, view: View? = null) {
    val clipboard = getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)

    if (Build.VERSION.SDK_INT < 33) {
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}

fun Context.hasSecondaryButton(): Boolean {
    val hasStemKeys = WearableButtons.getButtonCount(this) > 1
    return hasStemKeys && WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1) != null
}

fun Context.getSecondaryButtonLabel(): CharSequence? {
    return WearableButtons.getButtonLabel(this, KeyEvent.KEYCODE_STEM_1)
}

fun RecyclerView.applyRotaryInput() {
    // Ensure the view can receive focus to capture rotary events
    isFocusable = true
    isFocusableInTouchMode = true

    setOnGenericMotionListener { _, event ->
        if (event.action == MotionEvent.ACTION_SCROLL) {
            val axis: Int = MotionEvent.AXIS_SCROLL
            val isRotary: Boolean = event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)

            if (isRotary) {
                val delta = -event.getAxisValue(axis) *
                        ViewConfigurationCompat.getScaledVerticalScrollFactor(
                            ViewConfiguration.get(context), context
                        )
                scrollBy(0, delta.toInt())
                true
            } else false
        } else false
    }
}