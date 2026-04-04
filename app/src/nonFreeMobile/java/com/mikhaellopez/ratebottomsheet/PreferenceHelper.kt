package com.mikhaellopez.ratebottomsheet

import android.content.Context
import com.parseus.codecinfo.data.settingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * Copyright (C) 2020 Mikhael LOPEZ
 * Licensed under the Apache License Version 2.0
 */
class PreferenceHelper(context: Context) {

    private val settingsRepository = context.settingsRepository

    internal fun getInstallDays(): Long = runBlocking {
        settingsRepository.installDays.first()
    }

    internal fun setInstallDays() {
        runBlocking {
            settingsRepository.setInstallDays(Date().time)
        }
    }

    internal fun getCptLaunchTimes(): Int = runBlocking {
        settingsRepository.cptLaunchTimes.first()
    }

    internal fun setCptLaunchTimes() {
        runBlocking {
            val times = settingsRepository.cptLaunchTimes.first()
            settingsRepository.setCptLaunchTimes(times + 1)
        }
    }

    internal fun isAgreeShowBottomSheet(): Boolean = runBlocking {
        settingsRepository.isAgreeShowBottomSheet.first()
    }

    internal fun disableAgreeShowBottomSheet() {
        runBlocking {
            settingsRepository.setAgreeShowBottomSheet(false)
        }
    }

    internal fun getRemindInterval(): Long = runBlocking {
        settingsRepository.remindInterval.first()
    }

    internal fun setRemindInterval() {
        runBlocking {
            settingsRepository.setRemindInterval(Date().time)
        }
    }

    internal fun clear() {
        runBlocking {
            settingsRepository.clearRatePrefs()
        }
    }

}
