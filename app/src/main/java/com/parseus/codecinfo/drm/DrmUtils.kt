@file:RequiresApi(18)

package com.parseus.codecinfo.drm

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import androidx.annotation.RequiresApi
import com.parseus.codecinfo.R
import com.parseus.codecinfo.toHexString

fun getSimpleDrmInfoList(): List<DrmSimpleInfo> {
    return DrmVendor.values()
            .filter { MediaDrm.isCryptoSchemeSupported(it.uuid) }
            .mapNotNull { it.getIfSupported() }
}

fun getDetailedDrmInfo(context: Context, drmVendor: DrmVendor): Map<String, String> {
    val drmInfoMap = LinkedHashMap<String, String>()
    val mediaDrm = MediaDrm(drmVendor.uuid)

    drmInfoMap.addStringProperties(context, mediaDrm, DrmVendor.STANDARD_STRING_PROPERTIES)
    drmInfoMap.addByteArrayProperties(context, mediaDrm, DrmVendor.STANDARD_BYTE_ARRAY_PROPERTIES)

    drmInfoMap.addStringProperties(context, mediaDrm, drmVendor.getVendorStringProperties())
    drmInfoMap.addByteArrayProperties(context, mediaDrm, drmVendor.getVendorByteArrayProperties())

    if (Build.VERSION.SDK_INT >= 28) {
        drmInfoMap[context.getString(R.string.drm_property_security_level)] =
                getReadableSecurityLevel(context, MediaDrm.getMaxSecurityLevel(), drmInfoMap)

        drmInfoMap[context.getString(R.string.drm_property_hdcp_level)] =
                getReadableHdcpLevel(context, mediaDrm.connectedHdcpLevel)
        drmInfoMap[context.getString(R.string.drm_property_max_hdcp_level)] =
                getReadableHdcpLevel(context, mediaDrm.maxHdcpLevel)

        drmInfoMap[context.getString(R.string.drm_property_max_sessions)] = mediaDrm.maxSessionCount.toString()
    }

    mediaDrm.closeDrmInstance()

    return drmInfoMap
}

private fun getReadableSecurityLevel(context: Context, securityLevel: Int, infoMap: Map<String, String>): String {
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

    // Combine vendor value with a framework value for a complete description.
    return if (infoMap.containsKey(slKey)) {
        context.getString(R.string.drm_security_level_combined_format, infoMap[slKey], slFrameworkValue)
    } else {
        context.getString(stringResId)
    }
}

private fun getReadableHdcpLevel(context: Context, hdcpLevel: Int): String {
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
    return context.getString(stringResId)
}

private fun DrmVendor.getIfSupported() = try {
    val mediaDrm = MediaDrm(uuid)
    DrmSimpleInfo(mediaDrm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION), uuid).also {
        mediaDrm.closeDrmInstance()
    }
} catch (e: Exception) {
    null
}

private fun MutableMap<String, String>.addStringProperties(context: Context,
                                                           mediaDrm: MediaDrm,
                                                           vendorProperties: Map<Int, String>?) {
    vendorProperties?.forEach { (key, value) ->
        try {
            val propertyValue = mediaDrm.getPropertyString(value)
            if (propertyValue.isNotEmpty()) {
                this[context.getString(key)] = propertyValue
            }
        } catch (e: Exception) {}
    }
}

private fun MutableMap<String, String>.addByteArrayProperties(context: Context,
                                                           mediaDrm: MediaDrm,
                                                           vendorProperties: Map<Int, String>?) {
    vendorProperties?.forEach { (key, value) ->
        try {
            val propertyValue = mediaDrm.getPropertyByteArray(value).toHexString()
            if (propertyValue.isNotEmpty()) {
                this[context.getString(key)] = propertyValue
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