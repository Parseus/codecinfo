package com.parseus.codecinfo.data.drm

import java.util.*

data class DrmSimpleInfo(val id: Long, val drmName: String, val drmUuid: UUID) {

    override fun toString(): String {
        return "$drmName (UUID: {$drmUuid})"
    }

}