package com.parseus.codecinfo.utils

import kotlinx.serialization.json.Json

val jsonInstance = Json {
    ignoreUnknownKeys = true
}