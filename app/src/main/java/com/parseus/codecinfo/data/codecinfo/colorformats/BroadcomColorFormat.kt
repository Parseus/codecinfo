@file:Suppress("unused", "EnumEntryName")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class BroadcomColorFormat(val value: Int) {

    OMX_COLOR_Format32bitABGR8888(0x7F000001),
    OMX_COLOR_Format8bitPalette(0x7F000002),
    OMX_COLOR_FormatYUVUV128(0x7F000003),
    OMX_COLOR_FormatRawBayer12bit(0x7F000004),
    OMX_COLOR_FormatBRCMEGL(0x7F000005),
    OMX_COLOR_FormatBRCMOpaque(0x7F000006),
    OMX_COLOR_FormatYVU420PackedPlanar(0x7F000007),
    OMX_COLOR_FormatYVU420PackedSemiPlanar(0x7F000008),
    OMX_COLOR_FormatRawBayer16bit(0x7F000009),
    OMX_COLOR_FormatYUV420_16PackedPlanar(0x7F00000A),
    OMX_COLOR_FormatYUVUV64_16(0x7F00000B),
    OMX_COLOR_FormatYUV420_10PackedPlanar(0x7F00000C),
    OMX_COLOR_FormatYUVUV64_10(0x7F00000D),
    OMX_COLOR_FormatYUV420_UVSideBySide(0x7F00000E);

    companion object {
        fun from(findValue: Int) = values().find { it.value == findValue }?.name
    }

}