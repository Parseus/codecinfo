package com.parseus.codecinfo.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R
import kotlinx.android.synthetic.main.settings_main.*

class SettingsActivity : AppCompatActivity(R.layout.settings_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportFragmentManager.beginTransaction().replace(R.id.content, SettingsFragment()).commit()
    }

    override fun finish() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_THEME_CHANGED, themeChanged)
            putExtra(FILTER_TYPE_CHANGED, filterTypeChanged)
            putExtra(SORTING_CHANGED, sortingChanged)
        })
        super.finish()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val darkTheme = findPreference<ListPreference>("dark_theme")
            darkTheme!!.setOnPreferenceChangeListener { _, newValue ->
                AppCompatDelegate.setDefaultNightMode(DarkTheme.getAppCompatValue(newValue as Int))

                themeChanged = true
                requireActivity().recreate()

                true
            }

            val filterType = findPreference<ListPreference>("filter_type")
            filterType!!.setOnPreferenceChangeListener { _, _ ->
                filterTypeChanged = true
                true
            }

            val sortingType = findPreference<ListPreference>("sort_type")
            sortingType!!.setOnPreferenceChangeListener { _, _ ->
                sortingChanged = true
                true
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences_screen)
        }

        @SuppressLint("InflateParams")
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            return when (preference.key) {
                "feedback" -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${getString(R.string.feedback_email)}")
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
                    } else {
                        Snackbar.make(requireActivity().findViewById<View>(android.R.id.content),
                                R.string.no_email_apps, Snackbar.LENGTH_LONG).show()
                    }
                    true
                }

                "help" -> {
                    val builder = AlertDialog.Builder(requireActivity())
                    val dialogView = layoutInflater.inflate(R.layout.about_app_dialog, null)
                    builder.setView(dialogView)
                    val alertDialog = builder.create()

                    dialogView.findViewById<View>(R.id.ok_button).setOnClickListener { alertDialog.dismiss() }

                    try {
                        val versionTextView: TextView = dialogView.findViewById(R.id.version_text_view)
                        versionTextView.text = getString(R.string.app_version,
                                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName)
                    } catch (e : Exception) {}

                    alertDialog.show()
                    true
                }

                else -> super.onPreferenceTreeClick(preference)
            }
        }

    }

    companion object {
        var themeChanged = false
        var filterTypeChanged = false
        var sortingChanged = false
        const val EXTRA_THEME_CHANGED = "theme_changed"
        const val FILTER_TYPE_CHANGED = "filter_type_changed"
        const val SORTING_CHANGED = "sorting_changed"
    }

}