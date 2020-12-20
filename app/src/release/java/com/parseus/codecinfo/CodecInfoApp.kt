package com.parseus.codecinfo

import android.app.Application
import com.parseus.codecinfo.memoryleakfixes.IMMLeaks

class CodecInfoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        IMMLeaks.fixFocusedViewLeak(this)
        IMMLeaks.fixCurRootViewLeak(this)
    }

}