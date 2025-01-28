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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyUp(keyCode, event)
        }

        if (event != null && keyCode != KeyEvent.KEYCODE_ENTER) {
            buffer += event.unicodeChar.toChar()
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            callbackAfterScan?.let {
                it(buffer) // Передаем результат в callback
            } ?: throw NotImplementedError("callbackAfterScan isn't set! Use setCallbackAfterScan.")
            buffer = "" // Очищаем буфер
            return true // Возвращаем true, чтобы остановить дальнейшую обработку Enter
        }

        return super.onKeyUp(keyCode, event)
    }
}