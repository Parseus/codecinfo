@file:Suppress("EnumEntryName")

package com.parseus.codecinfo.codecinfo.profilelevels

enum class MVCProfiles(val value: Int) {

    MVCProfileStereoHigh(0x01),
    MVCProfileMultiViewHigh(0x02),
    MVCProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = MVCProfiles.values().find { it.value == findValue }?.name
    }

}

enum class MVCLevels (val value: Int) {

    MVCLevel1(0x01),
    MVCLevel1b(0x02),
    MVCLevel11(0x04),
    MVCLevel12(0x08),
    MVCLevel13(0x10),
    MVCLevel2(0x20),
    MVCLevel21(0x40),
    MVCLevel22(0x80),
    MVCLevel3(0x100),
    MVCLevel31(0x200),
    MVCLevel32(0x400),
    MVCLevel4(0x800),
    MVCLevel41(0x1000),
    MVCLevel42(0x2000),
    MVCLevel5(0x4000),
    MVCLevel51(0x8000),
    MVCLevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = MVCLevels.values().find { it.value == findValue }?.name
    }
}