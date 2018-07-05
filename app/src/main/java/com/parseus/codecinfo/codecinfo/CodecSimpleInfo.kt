package com.parseus.codecinfo.codecinfo

data class CodecSimpleInfo(val codecId: String,
                           val codecName: String,
                           val isAudio: Boolean) {

    override fun toString(): String {
        return "$codecId ($codecName)"
    }

}