package com.parseus.codecinfo.codecinfo.colorformats

@Suppress("EnumEntryName")
enum class MediaTekColorFormat(val value: Int) {
    OMX_COLOR_FormatVendorMTKYUV(0x7F000001),
    OMX_COLOR_FormatVendorMTKYUV_FCM(0x7F000002),
    OMX_COLOR_FormatVendorMTKYUV_10BIT_H(0x7F000004),
    OMX_COLOR_FormatVendorMTKYUV_10BIT_V(0x7F000005),
    OMX_COLOR_FormatVendorMTKYUV_UFO_10BIT_H(0x7F000006),
    OMX_COLOR_FormatVendorMTKYUV_UFO_10BIT_V(0x7F000007),
    OMX_MTK_COLOR_FormatYV12(0x7F000200),
    OMX_MTK_COLOR_FormatBitStream(0x7F000300);

    companion object {
        fun from(findValue: Int): String? = MediaTekColorFormat.values().find { it.value == findValue }?.name
    }

}