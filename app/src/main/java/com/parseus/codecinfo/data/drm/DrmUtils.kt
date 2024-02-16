package com.parseus.codecinfo.data.drm

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.utils.toHexString
import java.util.*

val drmList: MutableList<DrmSimpleInfo> = arrayListOf()
val detailedDrmInfo: MutableMap<UUID, List<DetailsProperty>> = mutableMapOf()

fun getSimpleDrmInfoList(context: Context): List<DrmSimpleInfo> {
    return drmList.ifEmpty {
        val list = mutableListOf<DrmSimpleInfo>()
        if (Build.VERSION.SDK_INT >= 30) {
            val supported = MediaDrm.getSupportedCryptoSchemes()
            for (uuid in supported) {
                val vendor = DrmVendor.entries.find { it.uuid == uuid }
                if (vendor != null) {
                    list.add(vendor.getSimpleInfo(context))
                } else {
                    val drmName = getDrmDescriptionFromUuid(uuid, context)
                    list.add(DrmSimpleInfo(list.size.toLong(), drmName, uuid))
                }
            }
        } else {
            DrmVendor.entries.forEach {
                try {
                    // This can crash in native code if something goes wrong while querying it.
                    val schemeSupported = MediaDrm.isCryptoSchemeSupported(it.uuid)
                    if (schemeSupported) {
                        list.add(it.getSimpleInfo(context))
                    }
                } catch (_: Throwable) {}
            }
        }
        list.sortedBy { it.drmName }
    }
}

fun isDetailedDrmInfoCached(uuid: UUID) = detailedDrmInfo[uuid] != null

fun getDetailedDrmInfo(context: Context, uuid: UUID, drmVendor: DrmVendor?): List<DetailsProperty> {
    if (detailedDrmInfo[uuid] != null) {
        return detailedDrmInfo[uuid]!!.also {
            saveToLogcat(context, uuid, drmVendor, it)
        }
    }

    val drmPropertyList = mutableListOf<DetailsProperty>()
    val mediaDrm = MediaDrm(uuid)

    drmPropertyList.add(DetailsProperty(0L, context.getString(R.string.drm_property_uuid), uuid.toString()))

    drmPropertyList.addStringProperties(context, mediaDrm, DrmVendor.STANDARD_STRING_PROPERTIES)
    drmPropertyList.addByteArrayProperties(context, mediaDrm, DrmVendor.STANDARD_BYTE_ARRAY_PROPERTIES)

    if (drmVendor != null) {
        drmPropertyList.addStringProperties(context, mediaDrm, drmVendor.getVendorStringProperties())
        if (drmVendor == DrmVendor.Widevine) {
            try {
                val decryptHashSupportString = context.getString(R.string.drm_property_decrypt_hash_support)
                val propertyValue = mediaDrm.getPropertyString("decryptHashSupport")
                if (propertyValue.isNotEmpty()) {
                    drmPropertyList.add(DetailsProperty(drmPropertyList.size.toLong(),
                        decryptHashSupportString, getHashFunctionDescriptionForWidevine(context, propertyValue)))
                }
            } catch (_: Throwable) {}
        }
        drmPropertyList.addByteArrayProperties(context, mediaDrm, drmVendor.getVendorByteArrayProperties())
    }

    // These can crash in native code if something goes wrong while querying it.
    if (Build.VERSION.SDK_INT >= 28) {
        val connectedHdcpLevel = try {
            mediaDrm.connectedHdcpLevel
        } catch (e: Exception) {
            MediaDrm.HDCP_LEVEL_UNKNOWN
        }
        addReadableHdcpLevel(context, connectedHdcpLevel,
            context.getString(R.string.drm_property_hdcp_level), drmPropertyList)

        val maxHdcpLevel = try {
            mediaDrm.maxHdcpLevel
        } catch (e: Exception) {
            MediaDrm.HDCP_LEVEL_UNKNOWN
        }
        addReadableHdcpLevel(context, maxHdcpLevel,
            context.getString(R.string.drm_property_max_hdcp_level), drmPropertyList)

        val maxSessionCount = try {
            mediaDrm.maxSessionCount
        } catch (e: Exception) {
            0
        }
        if (maxSessionCount > 0) {
            val maxSessionsEntry = drmPropertyList.find {
                it.name == context.getString(R.string.drm_property_max_sessions)
            }
            if (maxSessionsEntry != null) {
                val index = drmPropertyList.indexOf(maxSessionsEntry)
                drmPropertyList.remove(maxSessionsEntry)
                maxSessionsEntry.value = maxSessionCount.toString()
                drmPropertyList.add(index, maxSessionsEntry)
            } else {
                drmPropertyList.add(DetailsProperty(drmPropertyList.size.toLong(),
                    context.getString(R.string.drm_property_max_sessions), maxSessionCount.toString()))
            }
        }
    }

    mediaDrm.closeDrmInstance()

    detailedDrmInfo[uuid] = drmPropertyList

    return drmPropertyList.also {
        saveToLogcat(context, uuid, drmVendor, it)
    }
}

private fun addReadableHdcpLevel(context: Context, hdcpLevel: Int, key: String,
                                 propertyList: MutableList<DetailsProperty>) {
    val stringResId = when (hdcpLevel) {
        MediaDrm.HDCP_NONE              -> R.string.hdcp_level_none
        MediaDrm.HDCP_V1                -> R.string.hdcp_level_v1
        MediaDrm.HDCP_V2                -> R.string.hdcp_level_v2
        MediaDrm.HDCP_V2_1              -> R.string.hdcp_level_v2_1
        MediaDrm.HDCP_V2_2              -> R.string.hdcp_level_v2_2
        MediaDrm.HDCP_V2_3              -> R.string.hdcp_level_v2_3
        MediaDrm.HDCP_NO_DIGITAL_OUTPUT -> R.string.hdcp_level_no_digital_output
        else                            -> R.string.hdcp_level_unknown
    }

    // Prefer the framework property value over the vendor one.
    val existingEntry = propertyList.find { it.name == key }
    if (existingEntry != null) {
        val index = propertyList.indexOf(existingEntry)
        propertyList.remove(existingEntry)
        existingEntry.value = context.getString(stringResId)
        propertyList.add(index, existingEntry)
    } else {
        propertyList.add(DetailsProperty(propertyList.size.toLong(), key, context.getString(stringResId)))
    }
}

private fun DrmVendor.getSimpleInfo(context: Context): DrmSimpleInfo {
    val drmName = if (properNameResId == -1) {
        getDrmDescriptionFromUuid(uuid, context)
    } else {
        context.getString(properNameResId)
    }
    return DrmSimpleInfo(ordinal.toLong(), drmName, uuid)
}

private fun getDrmDescriptionFromUuid(uuid: UUID, context: Context) = try {
    val mediaDrm = MediaDrm(uuid)
    val drmName = mediaDrm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION)
    mediaDrm.closeDrmInstance()

    drmName
} catch (t: Throwable) {
    context.getString(R.string.unknown)
}

private fun MutableList<DetailsProperty>.addStringProperties(context: Context,
                                                            mediaDrm: MediaDrm,
                                                            vendorProperties: Map<Int, String>?) {
    vendorProperties?.forEach { (key, value) ->
        try {
            val propertyValue = mediaDrm.getPropertyString(value)
            if (propertyValue.isNotEmpty()) {
                add(DetailsProperty(size.toLong(), context.getString(key), propertyValue))
            }
        } catch (_: Throwable) {}
    }
}

private fun MutableList<DetailsProperty>.addByteArrayProperties(context: Context,
                                                            mediaDrm: MediaDrm,
                                                            vendorProperties: Map<Int, String>?) {
    vendorProperties?.forEach { (key, value) ->
        try {
            val propertyValue = mediaDrm.getPropertyByteArray(value).toHexString()
            if (propertyValue.isNotEmpty()) {
                add(DetailsProperty(size.toLong(), context.getString(key), propertyValue))
            }
        } catch (_: Throwable) {}
    }
}

private fun getHashFunctionDescriptionForWidevine(context: Context, value: String): String {
    return when (value.toInt()) {
        0 -> context.getString(R.string.decrypt_hash_support_not_supported)
        1 -> context.getString(R.string.decrypt_hash_support_crc)
        2 -> context.getString(R.string.decrypt_hash_support_partner)
        else -> "${context.getString(R.string.unknown)} ($value)"
    }
}

private fun MediaDrm.closeDrmInstance() {
    if (Build.VERSION.SDK_INT >= 28) {
        close()
    } else {
        @Suppress("DEPRECATION")
        release()
    }
}

private fun saveToLogcat(context: Context, drmUUID: UUID, drmVendor: DrmVendor?, detailsList: List<DetailsProperty>) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val saveDetailsToLogcat = prefs.getBoolean("save_details_to_logcat", false)
    if (saveDetailsToLogcat) {
        if (drmVendor != null) {
            Log.i("DrmUtils", "DRM: ${drmVendor.getSimpleInfo(context)}")
        } else {
            Log.i("DrmUtils", "DRM UUID: $drmUUID")
        }
        Log.i("DrmUtils", detailsList.joinToString("\n"))
    }
}