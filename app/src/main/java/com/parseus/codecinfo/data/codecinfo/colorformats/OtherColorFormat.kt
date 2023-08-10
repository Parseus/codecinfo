@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class OtherColorFormat(val value: Int) {

    // SPRD / PacketVideo
    OMX_SPRD_COLOR_FormatYVU420SemiPlanar(0x7FD00001),

    // Intel
    OMX_INTEL_COLOR_FormatHalYV12(0x32315659),
    OMX_INTEL_COLOR_FormatYUV420PackedSemiPlanar(0x7FA00E00),
    OMX_INTEL_COLOR_FormatYUV420PackedSemiPlanar_Tiled(0x7FA00F00),

    // Rockchip
    OMX_RX_COLOR_FormatAVCData(0x7FFFFFFE),

    // Texas Instruments
    OMX_TI_COLOR_FormatYUV420PackedSemiPlanarInterlaced(0x7F000001),
    OMX_TI_COLOR_FormatRawBayer10bitStereo(0x7F000002),
    OMX_TI_COLOR_FormatYUV420PackedSemiPlanar(0x7F000100);

    companion object {
        fun from(findValue: Int) = values().find { it.value == findValue }?.name
    }

}