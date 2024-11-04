@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class APVProfiles(val value: Int) {

    APVProfile422_10(0x01),
    APVProfile422_10HDR10(0x1000),
    APVProfile422_10HDR10Plus(0x2000),
    APVProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}

enum class APVLevels (val value: Int) {

    APVLevel1Band0(0x101),
    APVLevel1Band1(0x102),
    APVLevel1Band2(0x104),
    APVLevel1Band3(0x108),
    APVLevel11Band0(0x201),
    APVLevel11Band1(0x202),
    APVLevel11Band2(0x204),
    APVLevel11Band3(0x208),
    APVLevel2Band0(0x401),
    APVLevel2Band1(0x402),
    APVLevel2Band2(0x404),
    APVLevel2Band3(0x408),
    APVLevel21Band0(0x801),
    APVLevel21Band1(0x802),
    APVLevel21Band2(0x804),
    APVLevel21Band3(0x808),
    APVLevel3Band0(0x1001),
    APVLevel3Band1(0x1002),
    APVLevel3Band2(0x1004),
    APVLevel3Band3(0x1008),
    APVLevel31Band0(0x2001),
    APVLevel31Band1(0x2002),
    APVLevel31Band2(0x2004),
    APVLevel31Band3(0x2008),
    APVLevel4Band0(0x4001),
    APVLevel4Band1(0x4002),
    APVLevel4Band2(0x4004),
    APVLevel4Band3(0x4008),
    APVLevel41Band0(0x8001),
    APVLevel41Band1(0x8002),
    APVLevel41Band2(0x8004),
    APVLevel41Band3(0x8008),
    APVLevel5Band0(0x10001),
    APVLevel5Band1(0x10002),
    APVLevel5Band2(0x10004),
    APVLevel5Band3(0x10008),
    APVLevel51Band0(0x20001),
    APVLevel51Band1(0x20002),
    APVLevel51Band2(0x20004),
    APVLevel51Band3(0x20008),
    APVLevel6Band0(0x40001),
    APVLevel6Band1(0x40002),
    APVLevel6Band2(0x40004),
    APVLevel6Band3(0x40008),
    APVLevel61Band0(0x80001),
    APVLevel61Band1(0x80002),
    APVLevel61Band2(0x80004),
    APVLevel61Band3(0x80008),
    APVLevel7Band0(0x100001),
    APVLevel7Band1(0x100002),
    APVLevel7Band2(0x100004),
    APVLevel7Band3(0x100008),
    APVLevel71Band0(0x200001),
    APVLevel71Band1(0x200002),
    APVLevel71Band2(0x200004),
    APVLevel71Band3(0x200008),
    APVLevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }
}