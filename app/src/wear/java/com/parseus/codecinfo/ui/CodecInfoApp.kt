package com.parseus.codecinfo.ui

import android.app.Application
import com.parseus.codecinfo.data.settingsRepository

class CodecInfoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Trigger DataStore initialization early
        settingsRepository
    }

}