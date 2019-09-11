@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.codecinfo.colorformats

enum class MediaTekColorFormat(val value: Int) {
    COLOR_MTK_FormatYUVPrivate(0x32315679),
    OMX_COLOR_FormatVendorMTKYUV(0x7F000001),
    OMX_COLOR_FormatVendorMTKYUV_FCM(0x7F000002),
    OMX_COLOR_FormatVendorMTKYUV_UFO(0x7F000003),
    OMX_COLOR_FormatVendorMTKYUV_10BIT_H(0x7F000004),
    OMX_COLOR_FormatVendorMTKYUV_10BIT_V(0x7F000005),
    OMX_COLOR_FormatVendorMTKYUV_UFO_10BIT_H(0x7F000006),
    OMX_COLOR_FormatVendorMTKYUV_UFO_10BIT_V(0x7F000007),
    OMX_MTK_COLOR_FormatYV12(0x7F000200),
    OMX_MTK_COLOR_FormatBitStream(0x7F000300);

    companion object {
        fun from(findValue: Int) = values().find { it.value == findValue }?.name
    }

}