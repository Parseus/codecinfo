package com.parseus.codecinfo.ui.adapters

import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.knownproblems.KnownProblem

sealed class DetailItem {
    abstract val id: Long

    data class Header(
        override val id: Long,
        val headerTextResId: Int,
        val isExpanded: Boolean
    ) : DetailItem()

    data class KnownProblemItem(
        val problem: KnownProblem
    ) : DetailItem() {
        override val id: Long = problem.id + 1000000 // Offset to avoid conflict with properties
    }

    data class PropertyItem(
        val property: DetailsProperty
    ) : DetailItem() {
        override val id: Long = property.id
    }
}
