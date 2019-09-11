@file:Suppress("unused")

package com.parseus.codecinfo.codecinfo.profilelevels

enum class DolbyVisionProfiles(val value: Int) {

    DolbyVisionProfileDvavPer(0x1),
    DolbyVisionProfileDvavPen(0x2),
    DolbyVisionProfileDvheDer(0x4),
    DolbyVisionProfileDvheDen(0x8),
    DolbyVisionProfileDvheDtr(0x10),
    DolbyVisionProfileDvheStn(0x20),
    DolbyVisionProfileDvheDth(0x40),
    DolbyVisionProfileDvheDtb(0x80),
    DolbyVisionProfileDvheSt(0x100),
    DolbyVisionProfileDvavSe(0x200),
    DolbyVisionProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}

enum class DolbyVisionLevels(val value: Int) {

    DolbyVisionLevelHd24(0x1),
    DolbyVisionLevelHd30(0x2),
    DolbyVisionLevelFhd24(0x4),
    DolbyVisionLevelFhd30(0x8),
    DolbyVisionLevelFhd60(0x10),
    DolbyVisionLevelUhd24(0x20),
    DolbyVisionLevelUhd30(0x40),
    DolbyVisionLevelUhd48(0x80),
    DolbyVisionLevelUhd60(0x100),
    DolbyVisionLevelMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = values().find { it.value == findValue }?.name
    }

}