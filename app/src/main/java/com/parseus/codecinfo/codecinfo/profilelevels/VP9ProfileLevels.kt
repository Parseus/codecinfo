package com.parseus.codecinfo.codecinfo.profilelevels

enum class VP9Profiles(val value: Int) {

    VP9Profile0(0x01),
    VP9Profile1(0x02),
    VP9Profile2(0x04),
    VP9Profile3(0x08),
    VP9Profile2HDR(0x1000),
    VP9Profile3HDR(0x2000),
    VP9ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = VP9Profiles.values().find { it.value == findValue }?.name
    }

}

enum class VP9Levels(val value: Int) {
    VP9Level1(0x1),
    VP9Level11(0x2),
    VP9Level2(0x4),
    VP9Level21(0x8),
    VP9Level3(0x10),
    VP9Level31(0x20),
    VP9Level4(0x40),
    VP9Level41(0x80),
    VP9Level5(0x100),
    VP9Level51(0x200),
    VP9Level52(0x400),
    VP9Level6(0x800),
    VP9Level61(0x1000),
    VP9Level62(0x2000),
    VP9LevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = VP9Levels.values().find { it.value == findValue }?.name
    }
}