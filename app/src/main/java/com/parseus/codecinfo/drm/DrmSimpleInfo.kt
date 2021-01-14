package com.parseus.codecinfo.drm

import java.util.*

data class DrmSimpleInfo(val drmName: String, val drmUuid: UUID) {

    override fun toString(): String {
        return "$drmName (UUID: {$drmUuid})"
    }

}