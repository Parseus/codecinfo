@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class PanasonicSNIColorFormat(val value: Int) {

    OMX_COLOR_FormatAYUV8888(0x7F000001),
    OMX_COLOR_FormatRGBA8888L_BTC(0x7F000002),
    OMX_COLOR_FormatYUV10_TC_422PK(0x7F000003),
    OMX_COLOR_FormatYUV8_TC_422PK(0x7F000004),
    OMX_COLOR_FormatIndex8(0x7F000005),
    OMX_COLOR_Format16bitARGB3454(0x7F000006),
    OMX_COLOR_Format24bitGBR888(0x7F000007),
    OMX_COLOR_FormatYUV30_444PK(0x7F000008),
    OMX_COLOR_FormatYUV24_444PK(0x7F000009),
    OMX_COLOR_FormatYUV20_422PK(0x7F00000A),
    OMX_COLOR_FormatYUV16_422PK(0x7F00000B);

    companion object {
        fun from(findValue: Int) = entries.find { it.value == findValue }?.name
    }

}