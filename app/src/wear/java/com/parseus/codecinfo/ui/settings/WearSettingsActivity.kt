package com.parseus.codecinfo.ui.settings

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.settingsRepository
import com.parseus.codecinfo.utils.applyRotaryInput

class WearSettingsActivity : AppCompatActivity(R.layout.wear_activity_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<SwipeDismissFrameLayout>(R.id.swipeDismissRoot).addCallback(
            object : SwipeDismissFrameLayout.Callback() {
                override fun onDismissed(layout: SwipeDismissFrameLayout) {
                    finish()
                }
            }
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, WearSettingsFragment())
                .commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun finish() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ALIASES_CHANGED, aliasesChanged)
            putExtra(FILTER_TYPE_CHANGED, filterTypeChanged)
            putExtra(SORTING_CHANGED, sortingChanged)
            putExtra(HW_ICON_CHANGED, hwIconChanged)
            putExtra(SAVE_DETAILS_TO_LOGCAT_CHANGED, saveDetailsToLogcatChanged)
            putExtra(HW_ONLY_CODECS_CHANGED, hwOnlyCodecsChanged)
        })
        super.finish()

        // Reset flags
        aliasesChanged = false
        filterTypeChanged = false
        sortingChanged = false
        hwIconChanged = false
        saveDetailsToLogcatChanged = false
        hwOnlyCodecsChanged = false
    }

    class WearSettingsFragment : PreferenceFragmentCompat() {

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

            findPreference<ListPreference>("filter_type")?.setOnPreferenceChangeListener { _, _ ->
                filterTypeChanged = true
                true
            }

            findPreference<ListPreference>("sort_type")?.setOnPreferenceChangeListener { _, _ ->
                sortingChanged = true
                true
            }

            findPreference<CheckBoxPreference>("show_hw_icon")?.setOnPreferenceChangeListener { _, _ ->
                hwIconChanged = true
                true
            }

            findPreference<CheckBoxPreference>("save_details_to_logcat")?.setOnPreferenceChangeListener { _, _ ->
                saveDetailsToLogcatChanged = true
                true
            }

            findPreference<CheckBoxPreference>("show_hw_codecs_only")?.setOnPreferenceChangeListener { _, _ ->
                hwOnlyCodecsChanged = true
                true
            }
        }

        override fun onCreateRecyclerView(
            inflater: LayoutInflater,
            parent: ViewGroup,
            savedInstanceState: Bundle?
        ): RecyclerView {
            return WearableRecyclerView(inflater.context).apply {
                id = androidx.preference.R.id.recycler_view

                // FIX: Native way to prevent the last item from being cut off
                isEdgeItemsCenteringEnabled = true

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        override fun onCreateLayoutManager(): RecyclerView.LayoutManager {
            return WearableLinearLayoutManager(requireContext())
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Ensure a black background so white text is visible
            view.setBackgroundColor(Color.BLACK)

            listView.apply {
                layoutManager = WearableLinearLayoutManager(requireContext())

                clipToPadding = false
                val padding = (resources.displayMetrics.heightPixels * 0.3).toInt()
                setPadding(0, padding, 0, padding)

                applyRotaryInput()
                requestFocus()
            }
        }

        override fun onResume() {
            super.onResume()
            listView.requestFocus()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = requireContext().settingsRepository
            setPreferencesFromResource(R.xml.wear_preferences_screen, rootKey)
        }
    }

    companion object {
        var aliasesChanged = false
        var filterTypeChanged = false
        var sortingChanged = false
        var hwIconChanged = false
        var saveDetailsToLogcatChanged = false
        var hwOnlyCodecsChanged = false

        const val ALIASES_CHANGED = "aliases_changed"
        const val FILTER_TYPE_CHANGED = "filter_type_changed"
        const val SORTING_CHANGED = "sorting_changed"
        const val HW_ICON_CHANGED = "hw_icon_changed"
        const val SAVE_DETAILS_TO_LOGCAT_CHANGED = "save_details_to_logcat_changed"
        const val HW_ONLY_CODECS_CHANGED = "hw_only_codecs_changed"
    }

}