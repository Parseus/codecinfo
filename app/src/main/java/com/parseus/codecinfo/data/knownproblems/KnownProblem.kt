package com.parseus.codecinfo.data.knownproblems

import android.content.Context
import android.os.Build
import com.parseus.codecinfo.utils.isTv
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

var KNOWN_PROBLEMS_DB: List<KnownProblem> = emptyList()

@JsonClass(generateAdapter = true)
data class KnownProblem(
        val id: Long,
        @Json(name = "codec_name") val codecName: String,
        val description: String,
        val versions: List<Version>? = null,
        val devices: List<Device>? = null,
        val models: List<Model>? = null,
        val hardwares: List<Hardware>? = null,
        val urls: List<String>
) {

    fun isAffected(context: Context, codec: String): Boolean {
        // First case: codec name equality.
        if (!codec.equals(codecName, true)) {
            return false
        }

        val hardware = Build.HARDWARE
        var hardwareAffected = false
        hardwares?.forEach {
            if ((it.op == "equals" && hardware == it.value)
                    || (it.op == "startsWith" && hardware.startsWith(it.value))) {
                hardwareAffected = Build.HARDWARE.equals(it.value, true)
            }

            // Second case: devices that have no additional version requirement.
            if (hardwareAffected && versions == null) {
                return true
            }
        }

        val device = Build.DEVICE
        var deviceAffected = false
        devices?.forEach {
            if ((it.op == "equals" && device == it.value)
                    || (it.op == "startsWith" && device.startsWith(it.value))) {
                deviceAffected = if (it.manufacturer == null) {
                    true
                } else {
                    Build.MANUFACTURER.equals(it.manufacturer, true)
                }
            }

            // Third case: devices that have no additional version requirement.
            if (deviceAffected && versions == null) {
                return true
            }
        }

        val model = Build.MODEL
        var modelAffected = false
        models?.forEach {
            if ((it.op == "equals" && model == it.value)
                    || (it.op == "startsWith" && model.startsWith(it.value))) {
                modelAffected = if (it.manufacturer == null) {
                    true
                } else {
                    Build.MANUFACTURER.equals(it.manufacturer, true)
                }
            }

            // Fourth case: device models that have no additional version requirement.
            if (modelAffected && versions == null) {
                return true
            }
        }

        versions?.forEach { (op, value, value2, platform) ->
            if (("tv" == platform && !context.isTv())
                    || ("mobile" == platform && context.isTv())) {
                return@forEach
            }
            
            val deviceVersion = Build.VERSION.SDK_INT
            val versionAffected = when (op) {
                "=" -> deviceVersion == value
                ">=" -> deviceVersion >= value
                ">" -> deviceVersion > value
                "<=" -> deviceVersion <= value
                "<" -> deviceVersion < value
                "between" -> deviceVersion in value..(value2 ?: value)
                "!=" -> deviceVersion != value
                "all" -> true
                else -> false
            }

            if (((devices != null && deviceAffected) || (models != null && modelAffected)
                    || (hardwares != null && hardwareAffected)) && versionAffected) {
                // Fifth case: both device/model/hardware and version match.
                return true
            } else if (devices == null && models == null && versionAffected) {
                // Sixth case: version matches regardless of the device/model/hardware.
                return true
            }
        }

        // Still here? It either isn't affected or this a (currently) unknown case, then.
        return false
    }

}

@JsonClass(generateAdapter = true)
data class Version(
        val op: String,
        val value: Int = Int.MAX_VALUE,
        val value2: Int? = null,
        val platform: String? = null
)

@JsonClass(generateAdapter = true)
data class Device(
        val op: String,
        val value: String,
        val manufacturer: String? = null
)

@JsonClass(generateAdapter = true)
data class Model(
        val op: String,
        val value: String,
        val manufacturer: String? = null
)

@JsonClass(generateAdapter = true)
data class Hardware(
        val op: String,
        val value: String
)