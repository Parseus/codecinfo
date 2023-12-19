@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class SamsungColorFormat(val value: Int) {

    OMX_SEC_COLOR_FormatEncodedData(0x19),
    OMX_SEC_COLOR_FormatANBYUV420SemiPlanar(0x100),
    OMX_SEC_COLOR_FormatNV12TPhysicalAddress(0x7F000001),
    OMX_SEC_COLOR_FormatNV12LPhysicalAddress(0x7F000002),
    OMX_SEC_COLOR_FormatNV12LVirtualAddress(0x7F000003),
    OMX_SEC_COLOR_FormatNV21LPhysicalAddress(0x7F000010),
    OMX_SEC_COLOR_FormatNV21Linear(0x7F000011),
    OMX_SEC_COLOR_FormatYVU420Planar(0x7F000012),
    OMX_SEC_COLOR_Format32bitABGR8888(0x7F000013),
    OMX_SEC_COLOR_FormatYUV420SemiPlanarInterlace(0x7F000014),
    OMX_SEC_COLOR_Format10bitYUV420SemiPlanar(0x7F000015),
    OMX_SEC_COLOR_FormatS10bitYUV420SemiPlanar(0x7F000016),
    OMX_SEC_COLOR_Format10bitYVU420SemiPlanar(0x7F000017),
    OMX_SEC_COLOR_FormatS10bitYVU420SemiPlanar(0x7F000018),
    OMX_SEC_COLOR_FormatYUV420SemiPlanarSBWC(0x7F000019),
    OMX_SEC_COLOR_FormatYVU420SemiPlanarSBWC(0x7F000020),
    OMX_SEC_COLOR_Format10bitYUV420SemiPlanarSBWC(0x7F000021),
    OMX_SEC_COLOR_Format10bitYVU420SemiPlanarSBWC(0x7F000022),
    OMX_SEC_COLOR_FormatYUV420SemiPlanarSBWC_L50(0x7F000023),
    OMX_SEC_COLOR_FormatYUV420SemiPlanarSBWC_L75(0x7F000024),
    OMX_SEC_COLOR_Format10bitYUV420SemiPlanarSBWC_L40(0x7F000025),
    OMX_SEC_COLOR_Format10bitYUV420SemiPlanarSBWC_L60(0x7F000026),
    OMX_SEC_COLOR_Format10bitYUV420SemiPlanarSBWC_L80(0x7F000027),
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
        fun from(findValue: Int) = values().find { it.value == findValue }?.name
    }

}