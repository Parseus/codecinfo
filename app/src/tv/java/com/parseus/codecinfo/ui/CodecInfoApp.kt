package com.parseus.codecinfo.ui

import android.app.Application
import com.parseus.codecinfo.data.settingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CodecInfoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize DataStore and trigger migration if needed
        runBlocking { settingsRepository.settingsFlow.first() }
    }

}