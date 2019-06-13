package com.parseus.codecinfo.codecinfo.colorformats

@Suppress("EnumEntryName")
enum class MediaCodecColorFormat(val value: Int) {
    COLOR_FormatMonochrome(1),
    COLOR_Format16bitARGB1555(5),
    COLOR_Format16bitRGB565(6),
    COLOR_Format16bitBGR565(7),
    COLOR_Format18bitRGB666(8),
    COLOR_Format18bitARGB1665(9),
    COLOR_Format19bitARGB1666(10),
    COLOR_Format24bitRGB888(11),
    COLOR_Format24bitBGR888(12),
    COLOR_Format24bitARGB1887(13),
    COLOR_Format25bitARGB1888(14),
    COLOR_Format32bitBGRA8888(15),
    COLOR_Format32bitARGB8888(16),
    COLOR_FormatYUV411Planar(17),
    COLOR_FormatYUV411PackedPlanar(18),
    COLOR_FormatYUV420Planar(19),
    COLOR_FormatYUV420PackedPlanar(20),
    COLOR_FormatYUV420SemiPlanar (21),
    COLOR_FormatYUV422Planar(22),
    COLOR_FormatYUV422PackedPlanar(23),
    COLOR_FormatYUV422SemiPlanar (24),
    COLOR_FormatYCbYCr(25),
    COLOR_FormatYCrYCb(26),
    COLOR_FormatCbYCrY(27),
    COLOR_FormatCrYCbY(28),
    COLOR_FormatYUV444Interleaved(29),
    COLOR_FormatRawBayer8bit(30),
    COLOR_FormatRawBayer10bit(31),
    COLOR_FormatRawBayer8bitcompressed(32),
    COLOR_FormatL2(33),
    COLOR_FormatL4(34),
    COLOR_FormatL8(35),
    COLOR_FormatL16(36),
    COLOR_FormatL24(37),
    COLOR_FormatL32(38),
    COLOR_FormatYUV420PackedSemiPlanar(39),
    COLOR_FormatYUV422PackedSemiPlanar(40),
    COLOR_Format18BitBGR666(41),
    COLOR_Format24BitARGB6666(42),
    COLOR_Format24BitABGR6666(43),
    COLOR_FormatSurface (0x7F000789),
    COLOR_Format32bitABGR8888(0x7F00A000),
    COLOR_FormatYUV420Flexible(0x7F420888),
    COLOR_FormatYUV422Flexible(0x7F422888),
    COLOR_FormatYUV444Flexible(0x7F444888),
    COLOR_FormatRGBFlexible(0x7F36B888),
    COLOR_FormatRGBAFlexible (0x7F36A888),
    COLOR_QCOM_FormatYUV420SemiPlanar (0x7FA30C00),
    COLOR_TI_FormatYUV420PackedSemiPlanar(0x7F000100),

    // Other
    OMX_COLOR_FormatMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int) = values().find { it.value == findValue }?.name
    }

}