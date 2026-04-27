package com.parseus.codecinfo.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService
import com.parseus.codecinfo.R

fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)

    Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
}