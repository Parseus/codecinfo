package com.parseus.codecinfo.utils

import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R
import com.parseus.codecinfo.ui.settings.DarkTheme

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

fun Context.isNightMode(): Boolean {
    val appTheme = DarkTheme.fromValue(PreferenceManager.getDefaultSharedPreferences(this)
        .getString("dark_theme", getDefaultThemeOption(this).toString())!!.toInt()) ?: getDefaultThemeOption(this)
    return when (appTheme) {
        DarkTheme.Light -> false
        DarkTheme.Dark -> true
        else -> getSystemService<UiModeManager>()?.nightMode == UiModeManager.MODE_NIGHT_YES
    }
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
        val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("email", feedbackEmail))

        Snackbar.make(requireActivity().findViewById(android.R.id.content),
            R.string.no_email_apps_clipboard, Snackbar.LENGTH_LONG).show()
    }
}