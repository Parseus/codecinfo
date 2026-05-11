package com.parseus.codecinfo.data.knownproblems

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.parseus.codecinfo.BuildConfig
import com.parseus.codecinfo.R
import com.parseus.codecinfo.utils.isTv
import com.parseus.codecinfo.utils.jsonInstance
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream

var KNOWN_PROBLEMS_DB: List<KnownProblem> = emptyList()
var DEVICE_PROBLEMS_DB: List<KnownProblem> = emptyList()
var DATABASES_INITIALIZED = false

private val databaseMutex = Mutex()

@OptIn(ExperimentalSerializationApi::class)
suspend fun loadDatabases(context: Context) {
    if (DATABASES_INITIALIZED) return

    databaseMutex.withLock {
        if (DATABASES_INITIALIZED) return@withLock

        try {
            context.resources.openRawResource(R.raw.known_problems_list).use {
                KNOWN_PROBLEMS_DB = jsonInstance.decodeFromStream(it) ?: emptyList()
            }
        } catch (e: Exception) {
            KNOWN_PROBLEMS_DB = emptyList()
            if (BuildConfig.DEBUG) e.printStackTrace()
        }

        try {
            context.resources.openRawResource(R.raw.device_problem_list).use {
                DEVICE_PROBLEMS_DB = jsonInstance.decodeFromStream(it) ?: emptyList()
            }
        } catch (e: Exception) {
            DEVICE_PROBLEMS_DB = emptyList()
            if (BuildConfig.DEBUG) e.printStackTrace()
        }

        DATABASES_INITIALIZED = true
    }
}

private var sdkVersion: Int = -1
private var device: String? = null
private var model: String? = null
private var hardware: String? = null
private var socModel: String? = null
private var manufacturer: String? = null

@Serializable
data class KnownProblem(
    val id: Long,
    @SerialName("codec_name") val codecName: String? = null,
    val description: String,
    val versions: List<Version>? = null,
    val devices: List<Device>? = null,
    val models: List<Model>? = null,
    val hardwares: List<Hardware>? = null,
    val socModels: List<SoCModel>? = null,
    val manufacturers: List<Manufacturers>? = null,
    val urls: List<String>
) {

    @SuppressLint("NewApi")
    fun isAffected(context: Context, codec: String? = null): Boolean {
        if (!codec.equals(codecName, true)) {
            return false
        }

        if (sdkVersion == -1) {
            sdkVersion = Build.VERSION.SDK_INT
            device = Build.DEVICE
            model = Build.MODEL
            hardware = Build.HARDWARE
            socModel = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else null
            manufacturer = Build.MANUFACTURER
        }

        val hardwareAffected = hardwares?.any { matches(hardware!!, it.op, it.value) } ?: false
        if (hardwareAffected && versions == null) return true

        val deviceAffected = devices?.any {
            matches(device!!, it.op, it.value) &&
                    (it.manufacturer == null || manufacturer!!.equals(it.manufacturer, true))
        } ?: false
        if (deviceAffected && versions == null) return true

        val modelAffected = models?.any {
            matches(model!!, it.op, it.value) &&
                    (it.manufacturer == null || manufacturer!!.equals(it.manufacturer, true))
        } ?: false
        if (modelAffected && versions == null) return true

        val manufacturerAffected = manufacturers?.any { matches(manufacturer!!, it.op, it.value) } ?: false
        if (manufacturerAffected && versions == null) return true

        val socModelAffected = if (sdkVersion >= 31) {
            socModels?.any { matches(socModel!!, it.op, it.value) } ?: false
        } else false
        if (socModelAffected && versions == null) return true

        // If versions are null, and we haven't returned true yet, it's not affected
        val currentVersions = versions ?: return false

        val isTv = context.isTv()
        val anyCriteriaPresent = devices != null || models != null || hardwares != null ||
                manufacturers != null || socModels != null
        val anyCriteriaAffected = deviceAffected || modelAffected || hardwareAffected ||
                manufacturerAffected || socModelAffected

        return currentVersions.any { version ->
            if (("tv" == version.platform && !isTv) || ("mobile" == version.platform && isTv)) {
                return@any false
            }

            val versionMatch = when (version.op) {
                "=" -> sdkVersion == version.value
                ">=" -> sdkVersion >= version.value
                ">" -> sdkVersion > version.value
                "<=" -> sdkVersion <= version.value
                "<" -> sdkVersion < version.value
                "between" -> sdkVersion in version.value..(version.value2 ?: version.value)
                "!=" -> sdkVersion != version.value
                "all" -> true
                else -> false
            }

            // Version matches if:
            // 1. Both a specific criteria (device/model/etc.) AND version match
            // 2. OR no specific criteria are defined, and only the version matches
            versionMatch && (!anyCriteriaPresent || anyCriteriaAffected)
        }
    }

    private fun matches(actual: String, op: String, expected: String): Boolean {
        return when (op) {
            "equals" -> actual.equals(expected, ignoreCase = true)
            "startsWith" -> actual.startsWith(expected, ignoreCase = true)
            else -> false
        }
    }

}

@Serializable
data class Version(
        val op: String,
        val value: Int = Int.MAX_VALUE,
        val value2: Int? = null,
        val platform: String? = null
)

@Serializable
data class Device(
        val op: String,
        val value: String,
        val manufacturer: String? = null
)

@Serializable
data class Model(
        val op: String,
        val value: String,
        val manufacturer: String? = null
)

@Serializable
data class Hardware(
        val op: String,
        val value: String
)

@Serializable
data class SoCModel(
    val op: String,
    val value: String
)

@Serializable
data class Manufacturers(
    val op: String,
    val value: String
)