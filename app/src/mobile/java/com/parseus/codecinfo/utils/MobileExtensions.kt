package com.parseus.codecinfo.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R

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

fun Fragment.sendFeedbackEmail() {
    val feedbackEmail = getString(R.string.feedback_email)
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
    }
    if (intent.resolveActivity(requireActivity().packageManager) != null) {
        startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
    } else {
        // Setting text in a clipboard is wrapped to handle a bug in Android 4.3:
        // https://commonsware.com/blog/2013/08/08/developer-psa-please-fix-your-clipboard-handling.html
        try {
            val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
            clipboard?.setPrimaryClip(ClipData.newPlainText("email", feedbackEmail))

            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                R.string.no_email_apps_clipboard, Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                R.string.no_email_apps, Snackbar.LENGTH_LONG).show()
        }
    }
}