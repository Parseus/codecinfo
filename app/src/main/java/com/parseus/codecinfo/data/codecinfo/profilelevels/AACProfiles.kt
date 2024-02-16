@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

@Suppress("EnumEntryName")
enum class AACProfiles(val value: Int) {

    AACObjectMain(1),
    AACObjectLC(2),
    AACObjectSSR(3),
    AACObjectLTP(4),
    AACObjectHE(5),
    AACObjectScalable(6),
    AACObjectERLC(17),
    AACObjectERScalable(20),
    AACObjectLD(23),
    AACObjectHE_PS(29),
    AACObjectELD(39),
    AACObjectXHE(42);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}