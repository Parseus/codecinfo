package com.parseus.codecinfo.ui.settings

import android.os.Build
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.core.os.bundleOf
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.*
import com.parseus.codecinfo.R

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
            arguments = bundleOf(
                PREFERENCE_RESOURCE_ID to preferenceResId,
                PreferenceFragmentCompat.ARG_PREFERENCE_ROOT to root
            )
        }
    }

    class TvPreferenceFragment : LeanbackPreferenceFragmentCompat() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            findPreference<CheckBoxPreference>("show_aliases")?.apply {
                if (Build.VERSION.SDK_INT < 29) {
                    isVisible = false
                }
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