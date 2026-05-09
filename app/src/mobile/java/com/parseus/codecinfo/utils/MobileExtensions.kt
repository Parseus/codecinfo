package com.parseus.codecinfo.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R

fun Context.isInTwoPaneMode(): Boolean {
    return resources.getBoolean(R.bool.twoPaneMode)
}

fun Context.isNightMode(): Boolean {
    return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
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