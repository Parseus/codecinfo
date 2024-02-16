@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class QualcommColorFormat (val value: Int) {

    QOMX_COLOR_FormatYVU420SemiPlanar(0x7FA30C00),
    QOMX_COLOR_FormatYVU420PackedSemiPlanar32m4ka(0x7FA30C01),
    QOMX_COLOR_FormatYUV420PackedSemiPlanar16m2ka(0x7FA30C02),
    QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka(0x7FA30C03),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32m(0x7FA30C04),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32mMultiView(0x7FA30C05),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32mCompressed(0x7FA30C06),
    QOMX_COLOR_Format32bitRGBA8888(0x7FA30C07),
    QOMX_COLOR_Format32bitRGBA8888Compressed(0x7FA30C08),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32m10bitCompressed(0x7FA30C09),
    QOMX_COLOR_FORMATYUV420SemiPlanarP010Venus(0x7FA30C0A),
    QOMX_COLOR_FormatYUV420PackedSemiPlanar512m(0x7FA30C0B);

    companion object {
        fun from(findValue: Int) = entries.find { it.value == findValue }?.name
    }

}