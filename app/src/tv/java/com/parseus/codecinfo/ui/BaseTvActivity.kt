package com.parseus.codecinfo.ui

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.KeyboardShortcutInfo
import android.view.Menu
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.parseus.codecinfo.R
import com.parseus.codecinfo.ui.settings.TvSettingsActivity
import com.parseus.codecinfo.utils.copyToClipboard
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

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isCtrlPressed) {
            when (keyCode) {
                KeyEvent.KEYCODE_F -> {
                    startActivity(Intent(this, TvSearchActivity::class.java))
                    return true
                }
                KeyEvent.KEYCODE_S -> {
                    startActivity(Intent(this, TvShareActivity::class.java))
                    return true
                }
                KeyEvent.KEYCODE_COMMA -> {
                    startActivity(Intent(this, TvSettingsActivity::class.java))
                    return true
                }
                KeyEvent.KEYCODE_C -> {
                    val focusedView = window.currentFocus
                    val textToCopy = focusedView?.tag?.toString()
                    if (textToCopy != null) {
                        copyToClipboard(getString(R.string.app_name), textToCopy)
                    }
                    return true
                }
                KeyEvent.KEYCODE_V -> {
                    handlePaste()
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun handlePaste() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val textToPaste = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        if (textToPaste != null) {
            val cleanedText = textToPaste.replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "").trim()
            if (cleanedText.isNotEmpty()) {
                val intent = Intent(this, TvSearchActivity::class.java).apply {
                    putExtra("query", cleanedText)
                }
                startActivity(intent)
            }
        }
    }

    @RequiresApi(24)
    override fun onProvideKeyboardShortcuts(
        data: MutableList<KeyboardShortcutGroup>,
        menu: Menu?,
        deviceId: Int
    ) {
        val group = KeyboardShortcutGroup(getString(R.string.keyboard_shortcuts)).apply {
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_search), KeyEvent.KEYCODE_F, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_share), KeyEvent.KEYCODE_S, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_settings), KeyEvent.KEYCODE_COMMA, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_copy), KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON))
            addItem(KeyboardShortcutInfo(getString(R.string.keyboard_shortcut_paste), KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON))
        }
        data.add(group)
    }
}