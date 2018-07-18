package com.parseus.codecinfo.codecinfo.colorformats

@Suppress("EnumEntryName")
enum class QualcommColorFormat (val value: Int) {

    QOMX_COLOR_FormatYVU420PackedSemiPlanar32m4ka(0x7FA30C01),
    QOMX_COLOR_FormatYUV420PackedSemiPlanar16m2ka(0x7FA30C02),
    QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka(0x7FA30C03),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32m(0x7FA30C04),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32mMultiView(0x7FA30C05);
    //TODO: Find info about 0x7FA30C06

    companion object {
        fun from(findValue: Int): String? = QualcommColorFormat.values().find { it.value == findValue }?.name
    }

}