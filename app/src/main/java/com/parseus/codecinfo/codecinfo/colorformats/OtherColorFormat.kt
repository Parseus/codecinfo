package com.parseus.codecinfo.codecinfo.colorformats

@Suppress("EnumEntryName")
enum class OtherColorFormat(val value: Int) {

    // Sony
    OMX_STE_COLOR_FormatYUV420PackedSemiPlanarMB(0x7FA00000),

    // Texas Instruments
    OMX_TI_COLOR_FormatYUV420PackedSemiPlanarInterlaced(0x7F000001),
    OMX_TI_COLOR_FormatRawBayer10bitStereo(0x7F000002),

    ;

    companion object {
        fun from(findValue: Int): String? = OtherColorFormat.values().find { it.value == findValue }?.name
    }

}