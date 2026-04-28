package com.parseus.codecinfo.ui

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.VerticalGridPresenter

open class BaseVerticalGridSupportFragment : VerticalGridSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gridPresenter = VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_SMALL)
        gridPresenter.numberOfColumns = 1
        setGridPresenter(gridPresenter)
    }

}