package com.parseus.codecinfo.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.XmlRes
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.DialogPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.ui.settings.TvSettingsActivity.Companion.aliasesChanged
import com.parseus.codecinfo.ui.settings.TvSettingsActivity.Companion.filterTypeChanged
import com.parseus.codecinfo.ui.settings.TvSettingsActivity.Companion.hwIconChanged
import com.parseus.codecinfo.ui.settings.TvSettingsActivity.Companion.hwOnlyCodecsChanged
import com.parseus.codecinfo.ui.settings.TvSettingsActivity.Companion.saveDetailsToLogcatChanged
import com.parseus.codecinfo.ui.settings.TvSettingsActivity.Companion.sortingChanged

class TvSettingsFragment : LeanbackSettingsFragmentCompat(), DialogPreference.TargetFragment {

    private lateinit var preferenceFragment: PreferenceFragmentCompat

    override fun onPreferenceStartInitialScreen() {
        preferenceFragment = buildPreferenceFragment(R.xml.preferences_screen, null)
        startPreferenceFragment(preferenceFragment)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        return false
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
        val fragment = buildPreferenceFragment(R.xml.preferences_screen, pref.key)
        startPreferenceFragment(fragment)
        return true
    }

    override fun <T : Preference?> findPreference(key: CharSequence): T? = preferenceFragment.findPreference(key)

    private fun buildPreferenceFragment(@XmlRes preferenceResId: Int, root: String?): PreferenceFragmentCompat {
        return TvPreferenceFragment().apply {
            arguments = Bundle().apply {
                putInt(PREFERENCE_RESOURCE_ID, preferenceResId)
                putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, root)
            }
        }
    }

    class TvPreferenceFragment : LeanbackPreferenceFragmentCompat() {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            preferenceManager.preferenceDataStore = requireContext().settingsRepository
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            findPreference<CheckBoxPreference>("show_aliases")?.apply {
                if (Build.VERSION.SDK_INT >= 29) {
                    setOnPreferenceChangeListener { _, _ ->
                        aliasesChanged = true
                        true
                    }
                } else {
                    isVisible = false
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

            val showHwIcon = findPreference<CheckBoxPreference>("show_hw_icon")
            showHwIcon!!.setOnPreferenceChangeListener { _, _ ->
                hwIconChanged = true
                true
            }

            val saveDetailsToLogcat = findPreference<CheckBoxPreference>("save_details_to_logcat")
            saveDetailsToLogcat!!.setOnPreferenceChangeListener { _, _ ->
                saveDetailsToLogcatChanged = true
                true
            }

            val hwOnlyCodecs = findPreference<CheckBoxPreference>("show_hw_codecs_only")
            hwOnlyCodecs!!.setOnPreferenceChangeListener { _, _ ->
                hwOnlyCodecsChanged = true
                true
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val prefResId = requireArguments().getInt(PREFERENCE_RESOURCE_ID)

            if (rootKey == null) {
                addPreferencesFromResource(prefResId)
            } else {
                setPreferencesFromResource(prefResId, rootKey)
            }
        }

    }

    companion object {
        private const val PREFERENCE_RESOURCE_ID = "preferenceResource"
    }

}