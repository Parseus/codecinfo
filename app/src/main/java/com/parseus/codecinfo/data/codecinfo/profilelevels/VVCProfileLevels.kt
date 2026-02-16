@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class VVCProfiles(val value: Int) {

    VVCProfileUnknown(0x00),
    VVCProfileMain8(0x01),
    VVCProfileMain10(0x02),
    VVCProfileMain10Still(0x04),
    VVCProfileMain10HDR10(0x1000),
    VVCProfileMain10HDR10Plus(0x2000),
    VVCProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}

enum class VVCLevels(val value: Int) {

    VVCLevelUnknown(0x0),
    VVCMainTierLevel10(0x1),
    VVCMainTierLevel20(0x2),
    VVCMainTierLevel21(0x4),
    VVCMainTierLevel30(0x8),
    VVCMainTierLevel31(0x10),
    VVCMainTierLevel40(0x20),
    VVCHighTierLevel40(0x40),
    VVCMainTierLevel41(0x80),
    VVCHighTierLevel41(0x100),
    VVCMainTierLevel50(0x200),
    VVCHighTierLevel50(0x400),
    VVCMainTierLevel51(0x800),
    VVCHighTierLevel51(0x1000),
    VVCMainTierLevel52(0x2000),
    VVCHighTierLevel52(0x4000),
    VVCMainTierLevel60(0x8000),
    VVCHighTierLevel60(0x10000),
    VVCMainTierLevel61(0x20000),
    VVCHighTierLevel61(0x40000),
    VVCMainTierLevel62(0x80000),
    VVCHighTierLevel62(0x100000),
    VVCMainTierLevel63(0x200000),
    VVCHighTierLevel63(0x400000),
    VVCLevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}