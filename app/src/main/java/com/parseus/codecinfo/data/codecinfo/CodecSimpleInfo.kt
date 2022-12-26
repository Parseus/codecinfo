package com.parseus.codecinfo.data.codecinfo

data class CodecSimpleInfo(val id: Long,
                           val codecId: String,
                           val codecName: String,
                           val isAudio: Boolean,
                           val isEncoder: Boolean,
                           val isHardwareAccelereated: Boolean) {

    override fun toString(): String {
        return "$codecId ($codecName)"
    }

}