package com.parseus.codecinfo.utils

import com.parseus.codecinfo.BuildConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val jsonInstance = Json {
    ignoreUnknownKeys = true
    exceptionsWithDebugInfo = BuildConfig.DEBUG
}
