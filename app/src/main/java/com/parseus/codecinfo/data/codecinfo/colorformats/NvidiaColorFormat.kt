@file:Suppress("EnumEntryName", "unused")

package com.parseus.codecinfo.data.codecinfo.colorformats

enum class NvidiaColorFormat(val value: Int) {

    NVX_IMAGE_COLOR_FormatYV12(0x7F000001),
    NVX_IMAGE_COLOR_FormatNV21(0x7F000002),
    NVX_IMAGE_COLOR_FormatY(0x7F000003);

    companion object {
        fun from(findValue: Int) = entries.find { it.value == findValue }?.name
    }

}