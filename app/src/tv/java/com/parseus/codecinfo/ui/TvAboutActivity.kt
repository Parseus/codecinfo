package com.parseus.codecinfo.ui

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment

class TvAboutActivity : BaseTvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GuidedStepSupportFragment.addAsRoot(this, TvAboutFragment(), android.R.id.content)
    }

}