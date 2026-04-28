package com.parseus.codecinfo.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.content.getSystemService
import com.parseus.codecinfo.R

fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)

    if (Build.VERSION.SDK_INT < 33) {
        ToastCompat.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}