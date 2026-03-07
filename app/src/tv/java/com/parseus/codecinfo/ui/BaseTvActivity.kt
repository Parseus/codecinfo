package com.parseus.codecinfo.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.parseus.codecinfo.utils.canEnableMemoryLeakFixBackDispatcher
import com.parseus.codecinfo.utils.getMemoryLeakFixBackDispatcher

open class BaseTvActivity: FragmentActivity {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    private val memoryFixBackDispatcher = getMemoryLeakFixBackDispatcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.addOnBackStackChangedListener {
            memoryFixBackDispatcher.isEnabled = canEnableMemoryLeakFixBackDispatcher()
        }
        onBackPressedDispatcher.addCallback(memoryFixBackDispatcher)
    }
}