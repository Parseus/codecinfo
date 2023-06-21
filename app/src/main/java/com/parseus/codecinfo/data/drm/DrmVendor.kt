package com.parseus.codecinfo.data.drm

import android.media.MediaDrm
import androidx.annotation.RequiresApi
import com.parseus.codecinfo.R
import java.util.*

@RequiresApi(18)
enum class DrmVendor(val uuid: UUID,
                     val properNameResId: Int = -1) {

    AdobePrimeTime(UUID(-992507800680445872L, -7199381120623628549L)),
    CiscoNDSVideoGuard(UUID(6801168765294362723L, -8541191043631480108L)),
    ClearKey(UUID(0x1077EFECC0B24D02L, -0x531cc3e1ad1d04b5L),
            R.string.drm_clearkey_pssh_name),
    CoreCrypt(UUID(-5031789394971871680L, -5933022823712443662L)),
    DashIfClearKey(UUID(-2129748144642739255L, 8654423357094679310L),
            R.string.drm_clearkey_dash_if),
    /**
     * Not listed on DASH IF as a known DRM system; used by Netflix.
     * Apparently a CENC version of Apple's FairPlay,
     * although identifies itself as Widevine (?).
     * See: https://developer.apple.com/forums/thread/6185
     */
    FairPlayNetflix(UUID(2985921618079337012L, -8332874748677350841L),
            R.string.drm_fairplay_netflix_name),
    Irdeto(UUID(-7029884071836333870L, -9176437742005367753L)),
    Marlin(UUID(6801168765294362723L, -8541191043631480108L)),
    MobiTv(UUID(7681262094454315298L, -7309041776357940493L)),
    Nagra(UUID(-5930083867628189075L, -7670962396607644779L)),
    PlayReady(UUID(-0x65fb0f8667bfbd7aL, -0x546d19a41f77a06bL)),
    Verimatrix(UUID(-7338653513101981915L, -8305690818819724279L)),
    Widevine(UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)) {
        override fun getVendorStringProperties(): Map<Int, String> = WIDEVINE_STRING_PROPERTIES
        override fun getVendorByteArrayProperties(): Map<Int, String> = WIDEVINE_BYTE_ARRAY_PROPERTIES
    },
    WisePlay(UUID(0x3d5e6d359b9a41e8L, -0x47bc22c3918d3bd4L));

    open fun getVendorStringProperties(): Map<Int, String>? = null
    open fun getVendorByteArrayProperties(): Map<Int, String>? = null

    companion object {
        fun getFromUuid(uuid: UUID) = values().find { uuid == it.uuid }

        val STANDARD_STRING_PROPERTIES = mapOf(
                R.string.drm_property_vendor to MediaDrm.PROPERTY_VENDOR,
                R.string.drm_property_version to MediaDrm.PROPERTY_VERSION,
                R.string.drm_property_algorithms to MediaDrm.PROPERTY_ALGORITHMS
        )

        val STANDARD_BYTE_ARRAY_PROPERTIES = mapOf(
                R.string.drm_property_device_id to MediaDrm.PROPERTY_DEVICE_UNIQUE_ID,
        )

        val WIDEVINE_STRING_PROPERTIES = mapOf(
                R.string.drm_property_license_type to "licenseType",
                R.string.drm_property_security_level to "securityLevel",
                R.string.drm_property_system_id to "systemId",
                R.string.drm_property_app_id to "appId",
                R.string.drm_property_play_allowed to "playAllowed",
                R.string.drm_property_persist_allowed to "persistAllowed",
                R.string.drm_property_renew_allowed to "renewAllowed",
                R.string.drm_property_renewal_server_url to "renewalServerUrl",
                R.string.drm_property_origin to "origin",
                R.string.drm_property_privacy_mode to "privacyMode",
                R.string.drm_property_session_sharing to "sessionSharing",
                R.string.drm_property_usage_reporting_support to "usageReportingSupport",
                R.string.drm_property_oem_crypto_api_session_id to "oemCryptoSessionId",
                R.string.drm_property_oem_crypto_api_version to "oemCryptoApiVersion",
                R.string.drm_property_hdcp_level to "hdcpLevel",
                R.string.drm_property_max_hdcp_level to "maxHdcpLevel",
                R.string.drm_property_open_sessions to "numberOfOpenSessions",
                R.string.drm_property_max_sessions to "maxNumberOfSessions",
                R.string.drm_property_current_srm_version to "CurrentSRMVersion",
                R.string.drm_property_srm_update_support to "SRMUpdateSupport"
        )

        val WIDEVINE_BYTE_ARRAY_PROPERTIES = mapOf(
                R.string.drm_property_provisioning_id to "provisioningUniqueId",
                R.string.drm_property_service_certificate to "serviceCertificate"
        )
    }

}