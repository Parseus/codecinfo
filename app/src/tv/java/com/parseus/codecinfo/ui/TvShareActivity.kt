package com.parseus.codecinfo.ui

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment

class TvShareActivity : BaseTvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GuidedStepSupportFragment.addAsRoot(this, TvShareFragment(), android.R.id.content)
    }

}