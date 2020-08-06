package com.parseus.codecinfo.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class TvSettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, TvSettingsFragment()).commit()
    }

}