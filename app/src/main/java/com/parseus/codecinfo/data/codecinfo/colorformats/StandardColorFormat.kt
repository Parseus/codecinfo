@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.colorformats

/**
 * Combines color formats from MediaCodec, Stagefright and OpenMAX IL 1.2.0.
 */
enum class StandardColorFormat(val value: Int) {

    COLOR_FormatUnused(0x00),
    COLOR_FormatMonochrome(0x01),
    COLOR_Format8bitRGB332(0x02),
    COLOR_Format12bitRGB444(0x03),
    COLOR_Format16bitARGB4444(0x04),
    COLOR_Format16bitARGB1555(0x05),
    COLOR_Format16bitRGB565(0x06),
    COLOR_Format16bitBGR565(0x07),
    COLOR_Format18bitRGB666(0x08),
    COLOR_Format18bitARGB1665(0x09),
    COLOR_Format19bitARGB1666(0x0A),
    COLOR_Format24bitRGB888(0x0B),
    COLOR_Format24bitBGR888(0x0C),
    COLOR_Format24bitARGB1887(0x0D),
    COLOR_Format25bitARGB1888(0x0E),
    COLOR_Format32bitBGRA8888(0x0F),
    COLOR_Format32bitARGB8888(0x10),
    COLOR_FormatYUV411Planar(0x11),
    COLOR_FormatYUV411PackedPlanar(0x12),
    COLOR_FormatYUV420Planar(0x13),
    COLOR_FormatYUV420PackedPlanar(0x14),
    COLOR_FormatYUV420SemiPlanar(0x15),
    COLOR_FormatYUV422Planar(0x16),
    COLOR_FormatYUV422PackedPlanar(0x17),
    COLOR_FormatYUV422SemiPlanar(0x18),
    COLOR_FormatYCbYCr(0x19),
    COLOR_FormatYCrYCb(0x1A),
    COLOR_FormatCbYCrY(0x1B),
    COLOR_FormatCrYCbY(0x1C),
    COLOR_FormatYUV444Interleaved(0x1D),
    COLOR_FormatRawBayer8bit(0x1E),
    COLOR_FormatRawBayer10bit(0x1F),
    COLOR_FormatRawBayer8bitcompressed(0x20),
    COLOR_FormatL2(0x21),
    COLOR_FormatL4(0x22),
    COLOR_FormatL8(0x23),
    COLOR_FormatL16(0x24),
    COLOR_FormatL24(0x25),
    COLOR_FormatL32(0x26),
    COLOR_FormatYUV420PackedSemiPlanar(0x27),
    COLOR_FormatYUV422PackedSemiPlanar(0x28),
    COLOR_Format18BitBGR666(0x29),
    COLOR_Format24BitARGB6666(0x2A),
    COLOR_Format24BitABGR6666(0x2B),
    OMX_COLOR_Format32bitABGR8888(0x2C),
    COLOR_FormatYVU420Planar(0x2D),
    COLOR_FormatYVU420PackedPlanar(0x2E),
    COLOR_FormatYVU420SemiPlanar(0x2F),
    COLOR_FormatYVU420PackedSemiPlanar(0x30),
    COLOR_FormatYVU422Planar(0x31),
    COLOR_FormatYVU422PackedPlanar(0x32),
    COLOR_FormatYVU422SemiPlanar(0x33),
    COLOR_FormatYVU422PackedSemiPlanar(0x34),
    COLOR_Format8bitBGR233(0x35),
    COLOR_FormatYUVP010(0x36),
    COLOR_Format16bitBGRA4444(0x37),
    COLOR_Format16bitBGRA5551(0x38),
    COLOR_Format18bitBGRA5661(0x39),
    COLOR_Format19bitBGRA6661(0x3A),
    COLOR_Format24bitBGRA7881(0x3B),
    COLOR_Format25bitBGRA8881(0x3C),
    COLOR_Format24BitBGRA6666(0x3D),
    COLOR_Format24BitRGBA6666(0x3E),
    COLOR_FormatSurface (0x7F000789),
    COLOR_Format32bitABGR8888(0x7F00A000),
    COLOR_FormatRGBAFlexible (0x7F36A888),
    COLOR_FormatRGBFlexible(0x7F36B888),
    COLOR_FormatYUV420Planar16(0x7F42016B),
    COLOR_FormatYUV420Flexible(0x7F420888),
    COLOR_FormatYUV422Flexible(0x7F422888),
    COLOR_FormatYUV444Flexible(0x7F444888),
    COLOR_FormatYUV444Y410(0x7F444AAA),
    COLOR_Format32bitABGR2101010(0x7F00AAA2),
    COLOR_Format64bitABGRFloat(0x7F000F16),

    // https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/headers/media_plugin/media/openmax/OMX_IVCommon.h
    COLOR_QCOM_FormatYUV420SemiPlanar (0x7FA30C00),
    COLOR_QCOM_FormatYUV420PackedSemiPlanar64x32Tile2m8ka(0x7FA30C03),
    COLOR_QCOM_FormatYUV420PackedSemiPlanar32m(0x7FA30C04),
    COLOR_SEC_FormatNV12Tiled(0x7FC00002),
    COLOR_TI_FormatYUV420PackedSemiPlanar(0x7F000100),

    // Other
    OMX_COLOR_FormatMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int) = values().find { it.value == findValue }?.name
    }

}