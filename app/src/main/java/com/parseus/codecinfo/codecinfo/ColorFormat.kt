package com.parseus.codecinfo.codecinfo

@Suppress("EnumEntryName")
enum class ColorFormat(val value: Int) {
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

    // Color formats that are not defined in the MediaCodec class.

    // Sony
    OMX_STE_COLOR_FormatYUV420PackedSemiPlanarMB(0x7FA00000),

    // Qualcomm
    QOMX_COLOR_FormatYVU420PackedSemiPlanar32m4ka(0x7FA30C01),
    QOMX_COLOR_FormatYUV420PackedSemiPlanar16m2ka(0x7FA30C02),
    QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka(0x7FA30C03),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32m(0x7FA30C04),
    QOMX_COLOR_FORMATYUV420PackedSemiPlanar32mMultiView(0x7FA30C05),
    OMX_COLOR_FormatMax(0x7FFFFFFF),
    //TODO: Find info about 0x7FA30C06

    // Samsung
    OMX_SEC_COLOR_FormatANBYUV420SemiPlanar(0x100),
    OMX_SEC_COLOR_FormatNV12TPhysicalAddress(0x7F000001),
    OMX_SEC_COLOR_FormatNV12LPhysicalAddress(0x7F000002),
    OMX_SEC_COLOR_FormatNV12LVirtualAddress(0x7F000003),
    OMX_SEC_COLOR_FormatNV21LPhysicalAddress(0x7F000010),
    OMX_SEC_COLOR_FormatNV21Linear(0x7F000011),
    //TODO: Find info about 0x7F000012
    OMX_SEC_COLOR_FormatNV12Tiled(0x7FC00002),
    OMX_SEC_COLOR_FormatNV12Tiled_SBS_LR(0x7FC00003),
    OMX_SEC_COLOR_FormatNV12Tiled_SBS_RL(0x7FC00004),
    OMX_SEC_COLOR_FormatNV12Tiled_TB_LR(0x7FC00005),
    OMX_SEC_COLOR_FormatNV12Tiled_TB_RL(0x7FC00006),
    OMX_SEC_COLOR_FormatYUV420SemiPlanar_SBS_LR(0x7FC00007),
    OMX_SEC_COLOR_FormatYUV420SemiPlanar_SBS_RL(0x7FC00008),
    OMX_SEC_COLOR_FormatYUV420SemiPlanar_TB_LR(0x7FC00009),
    OMX_SEC_COLOR_FormatYUV420SemiPlanar_TB_RL(0x7FC0000A),
    OMX_SEC_COLOR_FormatYUV420Planar_SBS_LR(0x7FC0000B),
    OMX_SEC_COLOR_FormatYUV420Planar_SBS_RL(0x7FC0000C),
    OMX_SEC_COLOR_FormatYUV420Planar_TB_LR(0x7FC0000D),
    OMX_SEC_COLOR_FormatYUV420Planar_TB_RL(0x7FC0000E);

    companion object {
        fun from(findValue: Int): String? = try {
            ColorFormat.values().first { it.value == findValue }.name
        } catch (e: Exception) {
            null
        }
    }
}