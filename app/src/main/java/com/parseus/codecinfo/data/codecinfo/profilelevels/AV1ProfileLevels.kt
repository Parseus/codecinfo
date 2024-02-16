@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class AV1Profiles(val value: Int) {

    AV1ProfileMain8(0x01),
    AV1ProfileMain10(0x02),
    AV1ProfileMain10HDR10(0x1000),
    AV1ProfileMain10HDR10Plus(0x2000),

    AV1ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}

enum class AV1Levels (val value: Int) {

    AV1Level2(0x01),
    AV1Level21(0x02),
    AV1Level22(0x04),
    AV1Level23(0x08),
    AV1Level3(0x10),
    AV1Level31(0x20),
    AV1Level32(0x40),
    AV1Level33(0x80),
    AV1Level4(0x100),
    AV1Level41(0x200),
    AV1Level42(0x400),
    AV1Level43(0x800),
    AV1Level5(0x1000),
    AV1Level51(0x2000),
    AV1Level52(0x4000),
    AV1Level53(0x8000),
    AV1Level6(0x10000),
    AV1Level61(0x20000),
    AV1Level62(0x40000),
    AV1Level63(0x80000),
    AV1Level7(0x100000),
    AV1Level71(0x200000),
    AV1Level72(0x400000),
    AV1Level73(0x800000),
    AV1LevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }
}