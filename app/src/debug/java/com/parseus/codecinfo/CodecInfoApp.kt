package com.parseus.codecinfo

import androidx.multidex.MultiDexApplication
import com.parseus.codecinfo.memoryleakfixes.IMMLeaks

class CodecInfoApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        IMMLeaks.fixFocusedViewLeak(this)
        IMMLeaks.fixCurRootViewLeak(this)
    }

}