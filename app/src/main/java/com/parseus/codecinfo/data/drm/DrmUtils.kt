@file:RequiresApi(18)

package com.parseus.codecinfo.data.drm

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import androidx.annotation.RequiresApi
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.utils.toHexString

val drmList: MutableList<DrmSimpleInfo> = arrayListOf()

fun getSimpleDrmInfoList(context: Context): List<DrmSimpleInfo> {
    return if (drmList.isNotEmpty()) {
        drmList
    } else {
        val list = mutableListOf<DrmSimpleInfo>()
        DrmVendor.values().forEach {
            try {
                // This can crash in native code if something goes wrong while querying it.
                val schemeSupported = MediaDrm.isCryptoSchemeSupported(it.uuid)
                if (schemeSupported) {
                    val drmInfo = it.getIfSupported(context)
                    if (drmInfo != null) {
                        list.add(drmInfo)
                    }
                }
            } catch (e: Exception) {}
        }
        list
    }
}

fun getDetailedDrmInfo(context: Context, drmVendor: DrmVendor): List<DetailsProperty> {
    val drmPropertyList = mutableListOf<DetailsProperty>()
    val mediaDrm = MediaDrm(drmVendor.uuid)

    drmPropertyList.add(DetailsProperty(drmPropertyList.size.toLong(),
            context.getString(R.string.drm_property_uuid), drmVendor.uuid.toString()))

    drmPropertyList.addStringProperties(context, mediaDrm, DrmVendor.STANDARD_STRING_PROPERTIES)
    drmPropertyList.addByteArrayProperties(context, mediaDrm, DrmVendor.STANDARD_BYTE_ARRAY_PROPERTIES)

    drmPropertyList.addStringProperties(context, mediaDrm, drmVendor.getVendorStringProperties())
    drmPropertyList.addByteArrayProperties(context, mediaDrm, drmVendor.getVendorByteArrayProperties())

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

    return drmPropertyList
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

private fun DrmVendor.getIfSupported(context: Context) = try {
    val mediaDrm = MediaDrm(uuid)
    val drmName = if (properNameResId == -1) {
        mediaDrm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION)
    } else {
        context.getString(properNameResId)
    }
    DrmSimpleInfo(ordinal.toLong(), drmName, uuid).also {
        mediaDrm.closeDrmInstance()
    }
} catch (e: Exception) {
    null
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
        } catch (e: Exception) {}
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
        } catch (e: Exception) {}
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