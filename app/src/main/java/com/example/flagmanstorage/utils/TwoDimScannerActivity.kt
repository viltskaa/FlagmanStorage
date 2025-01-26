package com.example.flagmanstorage.utils

import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity

abstract class TwoDimScannerActivity : AppCompatActivity() {
    private var callbackAfterScan: ((str: String) -> Unit)? = null
    private var buffer: String = ""

    protected fun setCallbackAfterScan(callback: (str: String) -> Unit) {
        callbackAfterScan = callback
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            // Back button code
            return super.onKeyUp(keyCode, event)
        }

        return if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (callbackAfterScan != null) {
                callbackAfterScan?.let { it(buffer) }
            } else {
                throw NotImplementedError("callbackAfterScan isn't present!, use setCallbackAfterScan")
            }
            buffer = ""
            true
        } else {
            event?.let { buffer += it.unicodeChar.toChar() }
            super.onKeyUp(keyCode, event)
        }
    }
}