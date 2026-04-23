package com.parseus.codecinfo.ui

import android.content.Context
import androidx.startup.Initializer
import com.parseus.codecinfo.BuildConfig
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.knownproblems.DATABASES_INITIALIZED
import com.parseus.codecinfo.data.knownproblems.DEVICE_PROBLEMS_DB
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.utils.jsonInstance
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonDecodingException
import kotlinx.serialization.json.decodeFromStream

@Suppress("KotlinConstantConditions")
@OptIn(ExperimentalSerializationApi::class)
class DatabaseInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (DATABASES_INITIALIZED) return

        try {
            context.resources.openRawResource(R.raw.known_problems_list).use {
                KNOWN_PROBLEMS_DB = jsonInstance.decodeFromStream(it) ?: emptyList()
            }
        } catch (e: JsonDecodingException) {
            KNOWN_PROBLEMS_DB = emptyList()
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        } catch (_: Exception) {
            KNOWN_PROBLEMS_DB = emptyList()
        }

        try {
            context.resources.openRawResource(R.raw.device_problem_list).use {
                DEVICE_PROBLEMS_DB = jsonInstance.decodeFromStream(it) ?: emptyList()
            }
        } catch (e: JsonDecodingException) {
            DEVICE_PROBLEMS_DB = emptyList()
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        } catch (_: Exception) {
            DEVICE_PROBLEMS_DB = emptyList()
        }

        DATABASES_INITIALIZED = true
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}