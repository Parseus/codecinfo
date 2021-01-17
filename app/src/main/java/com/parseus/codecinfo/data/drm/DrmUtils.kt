@file:RequiresApi(18)

package com.parseus.codecinfo.data.drm

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import androidx.annotation.RequiresApi
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.utils.toHexString

private val drmList: MutableList<DrmSimpleInfo> = arrayListOf()

fun getSimpleDrmInfoList(): List<DrmSimpleInfo> {
    return if (drmList.isNotEmpty()) {
        drmList
    } else {
        DrmVendor.values()
                .filter { MediaDrm.isCryptoSchemeSupported(it.uuid) }
                .mapNotNull { it.getIfSupported() }
                .also { drmList.addAll(it) }
    }
}

fun getDetailedDrmInfo(context: Context, drmVendor: DrmVendor): List<DetailsProperty> {
    val drmPropertyList = mutableListOf<DetailsProperty>()
    val mediaDrm = MediaDrm(drmVendor.uuid)

    drmPropertyList.addStringProperties(context, mediaDrm, DrmVendor.STANDARD_STRING_PROPERTIES)
    drmPropertyList.addByteArrayProperties(context, mediaDrm, DrmVendor.STANDARD_BYTE_ARRAY_PROPERTIES)

    drmPropertyList.addStringProperties(context, mediaDrm, drmVendor.getVendorStringProperties())
    drmPropertyList.addByteArrayProperties(context, mediaDrm, drmVendor.getVendorByteArrayProperties())

    if (Build.VERSION.SDK_INT >= 28) {
        addReadableSecurityLevel(context, MediaDrm.getMaxSecurityLevel(), drmPropertyList)

        addReadableHdcpLevel(context, mediaDrm.connectedHdcpLevel,
                context.getString(R.string.drm_property_hdcp_level), drmPropertyList)
        addReadableHdcpLevel(context, mediaDrm.maxHdcpLevel,
                context.getString(R.string.drm_property_max_hdcp_level), drmPropertyList)

        val maxSessionCount = mediaDrm.maxSessionCount.toString()
        val maxSessionsEntry = drmPropertyList.find {
            it.name == context.getString(R.string.drm_property_max_sessions)
        }
        if (maxSessionsEntry != null) {
            val index = drmPropertyList.indexOf(maxSessionsEntry)
            drmPropertyList.remove(maxSessionsEntry)
            maxSessionsEntry.value = maxSessionCount
            drmPropertyList.add(index, maxSessionsEntry)
        } else {
            drmPropertyList.add(DetailsProperty(drmPropertyList.size.toLong(),
                    context.getString(R.string.drm_property_max_sessions), maxSessionCount))
        }
    }

    mediaDrm.closeDrmInstance()

    return drmPropertyList
}

private fun addReadableSecurityLevel(context: Context, securityLevel: Int,
                                     propertyList: MutableList<DetailsProperty>) {
    val stringResId = when (securityLevel) {
        MediaDrm.SECURITY_LEVEL_SW_SECURE_CRYPTO -> R.string.drm_security_level_sw_secure_crypto
        MediaDrm.SECURITY_LEVEL_SW_SECURE_DECODE -> R.string.drm_security_level_sw_secure_decode
        MediaDrm.SECURITY_LEVEL_HW_SECURE_CRYPTO -> R.string.drm_security_level_hw_secure_crypto
        MediaDrm.SECURITY_LEVEL_HW_SECURE_DECODE -> R.string.drm_security_level_hw_secure_decode
        MediaDrm.SECURITY_LEVEL_HW_SECURE_ALL    -> R.string.drm_security_level_hw_secure_all
        else                                     -> R.string.drm_security_level_unknown
    }

    val slKey = context.getString(R.string.drm_property_security_level)
    val slFrameworkValue = context.getString(stringResId)

    // Prefer the framework property value over the vendor one.
    val existingEntry = propertyList.find { it.name == slKey }
    if (existingEntry != null) {
        val index = propertyList.indexOf(existingEntry)
        propertyList.remove(existingEntry)
        existingEntry.value = context.getString(R.string.drm_security_level_combined_format, existingEntry.value, slFrameworkValue)
        propertyList.add(index, existingEntry)
    } else {
        propertyList.add(DetailsProperty(propertyList.size.toLong(), slKey, slFrameworkValue))
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

private fun DrmVendor.getIfSupported() = try {
    val mediaDrm = MediaDrm(uuid)
    DrmSimpleInfo(ordinal.toLong(), mediaDrm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION), uuid).also {
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