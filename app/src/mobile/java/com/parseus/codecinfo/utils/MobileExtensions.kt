package com.parseus.codecinfo.utils

import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.ui.settings.DarkTheme

fun Context.isInTwoPaneMode(): Boolean {
    return resources.getBoolean(R.bool.twoPaneMode)
}

fun Context.getAttributeDimension(@AttrRes attrResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
}

fun Context.getAttributeResourceId(@AttrRes attrResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return typedValue.resourceId
}

fun Context.isNightMode(): Boolean {
    val settings = settingsRepository.getSettingsSync()
    val appTheme = DarkTheme.fromValue(settings.darkTheme)
    return when (appTheme) {
        DarkTheme.Light -> false
        DarkTheme.Dark -> true
        else -> getSystemService<UiModeManager>()?.nightMode == UiModeManager.MODE_NIGHT_YES
    }
}

fun Context.copyToClipboard(label: String, text: String, view: View? = null) {
    val clipboard = getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)

    if (Build.VERSION.SDK_INT < 33 && view != null) {
        Snackbar.make(view, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
    }
}

fun Fragment.sendFeedbackEmail() {
    val feedbackEmail = getString(R.string.feedback_email)
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
    }
    if (intent.resolveActivity(requireActivity().packageManager) != null) {
        startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
    } else if (isAdded) {
        try {
            requireContext().copyToClipboard("email", feedbackEmail)

            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                R.string.no_email_apps_clipboard, Snackbar.LENGTH_LONG).show()
        } catch (_: Exception) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                R.string.no_email_apps, Snackbar.LENGTH_LONG).show()
        }
    }
}