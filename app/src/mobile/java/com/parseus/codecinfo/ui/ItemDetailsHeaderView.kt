package com.parseus.codecinfo.ui

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import com.google.android.material.textview.MaterialTextView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.utils.getSurfaceColor

@RequiresApi(21)
class ItemDetailsHeaderView : MaterialTextView {

    var isHeaderLifted: Boolean = false
        set(value) {
            field = value
            refreshDrawableState()
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    init {
        setBackgroundColor(getSurfaceColor(context))
        stateListAnimator = AnimatorInflater.loadStateListAnimator(context, R.animator.item_details_header_state_list_animator)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        return super.onCreateDrawableState(extraSpace + 1).also {
            if (isHeaderLifted) {
                mergeDrawableStates(it, STATE_LIFTED)
            }
        }
    }

    companion object {
        private val STATE_LIFTED = intArrayOf(R.attr.state_header_lifted)
    }

}