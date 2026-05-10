package com.parseus.codecinfo.ui

import android.app.Application
import androidx.tracing.trace
import com.parseus.codecinfo.data.settingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CodecInfoApp : Application() {

    override fun onCreate(): Unit = trace("CodecInfoApp.onCreate") {
        super.onCreate()
        // Initialize DataStore and trigger migration if needed
        runBlocking { settingsRepository.settingsFlow.first() }
    }

}