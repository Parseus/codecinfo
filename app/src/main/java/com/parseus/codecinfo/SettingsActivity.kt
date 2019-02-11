package com.parseus.codecinfo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.settings_main.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_main)
        setSupportActionBar(toolbar)
        supportFragmentManager.beginTransaction().replace(R.id.content, SettingsFragment()).commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences_screen)
        }

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

                else -> super.onPreferenceTreeClick(preference)
            }
        }

    }

}