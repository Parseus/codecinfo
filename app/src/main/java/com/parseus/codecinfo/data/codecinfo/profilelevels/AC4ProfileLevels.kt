@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

enum class AC4Profiles(val value: Int) {

    AC4Profile00(0x101),
    AC4Profile10(0x201),
    AC4Profile11(0x202),
    AC4Profile21(0x402),
    AC4Profile22(0x404),
    AC4ProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}

enum class AC4Levels(val value: Int) {

    AC4Level0(0x1),
    AC4Level1(0x2),
    AC4Level2(0x4),
    AC4Level3(0x8),
    AC4Level4(0x10),
    AC4LevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}