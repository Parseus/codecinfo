@file:Suppress("unused")

package com.parseus.codecinfo.data.codecinfo.profilelevels

@Suppress("EnumEntryName")
enum class WMAProfiles(val value: Int) {

    OMX_AUDIO_WMAProfileL1(0x01),
    OMX_AUDIO_WMAProfileL2(0x02),
    OMX_AUDIO_WMAProfileL3(0x03),
    OMX_AUDIO_WMAProfileM0(0x04),
    OMX_AUDIO_WMAProfileM1(0x05),
    OMX_AUDIO_WMAProfileM2(0x06),
    OMX_AUDIO_WMAProfileM3(0x07),
    OMX_AUDIO_WMAProfileN1(0x08),
    OMX_AUDIO_WMAProfileN2(0x09),
    OMX_AUDIO_WMAProfileN3(0x0A),
    OMX_AUDIO_WMAProfileS1(0x0B),
    OMX_AUDIO_WMAProfileS2(0x0C),
    OMX_AUDIO_WMAProfileMax(0x7FFFFFFF);

    companion object {
        fun from(findValue: Int): String? = entries.find { it.value == findValue }?.name
    }

}