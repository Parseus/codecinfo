package com.parseus.codecinfo.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.SettingsMainBinding
import com.parseus.codecinfo.getDefaultThemeOption
import com.parseus.codecinfo.isBatterySaverDisallowed

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = SettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportFragmentManager.beginTransaction().replace(R.id.content, SettingsFragment()).commit()
    }

    override fun finish() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(FILTER_TYPE_CHANGED, filterTypeChanged)
            putExtra(SORTING_CHANGED, sortingChanged)
            putExtra(IMMERSIVE_CHANGED, immersiveChanged)
        })
        super.finish()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            findPreference<CheckBoxPreference>("immersive_mode")?.apply {
                if (Build.VERSION.SDK_INT >= 19) {
                    setOnPreferenceChangeListener { _, _ ->
                        immersiveChanged = true
                        true
                    }
                } else {
                    isVisible = false
                }
            }

            findPreference<ListPreference>("dark_theme")!!.apply {
                setDarkThemeOptions(this)
                setOnPreferenceChangeListener { _, newValue ->
                    AppCompatDelegate.setDefaultNightMode(DarkTheme.getAppCompatValue((newValue as String).toInt()))
                    true
                }
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
                    val feedbackEmail = getString(R.string.feedback_email)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$feedbackEmail")
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
                    } else {
                        val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                        clipboard!!.setPrimaryClip(ClipData.newPlainText("email", feedbackEmail))

                        Snackbar.make(requireActivity().findViewById<View>(android.R.id.content),
                                R.string.no_email_apps, Snackbar.LENGTH_LONG).show()
                    }
                    true
                }

                "help" -> {
                    val builder = MaterialAlertDialogBuilder(requireActivity())
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

        private fun setDarkThemeOptions(listPreference: ListPreference) {
            val entries = mutableListOf<CharSequence>()
            val entryValues = mutableListOf<CharSequence>()

            // Light and Dark (always present)
            entries.add(getString(R.string.app_theme_light))
            entries.add(getString(R.string.app_theme_dark))
            entryValues.add(DarkTheme.Light.value.toString())
            entryValues.add(DarkTheme.Dark.value.toString())

            // Set by battery saver (if not blacklisted)
            if (!isBatterySaverDisallowed()) {
                entries.add(getString(R.string.app_theme_battery_saver))
                entryValues.add(DarkTheme.BatterySaver.value.toString())
            }

            // System default (Android 9.0+)
            if (Build.VERSION.SDK_INT >= 28) {
                entries.add(getString(R.string.app_theme_system_default))
                entryValues.add(DarkTheme.SystemDefault.value.toString())
            }

            listPreference.entries = entries.toTypedArray()
            listPreference.entryValues = entryValues.toTypedArray()
            listPreference.setDefaultValue(getDefaultThemeOption().toString())
        }

    }

    companion object {
        var filterTypeChanged = false
        var sortingChanged = false
        var immersiveChanged = false
        const val FILTER_TYPE_CHANGED = "filter_type_changed"
        const val SORTING_CHANGED = "sorting_changed"
        const val IMMERSIVE_CHANGED = "immersive_changed"
    }

}