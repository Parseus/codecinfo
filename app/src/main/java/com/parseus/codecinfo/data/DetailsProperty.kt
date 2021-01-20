package com.parseus.codecinfo.data

data class DetailsProperty(val id: Long, val name: String, var value: String) {

    override fun toString(): String {
        return if (value.contains('\n')) {
            "$name:\n$value"
        } else {
            "$name: $value"
        }
    }

}