package com.parseus.codecinfo.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class TvKeyboardShortcutsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val fragment = TvKeyboardShortcutsFragment()
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, fragment)
                .commit()
        }
    }
}