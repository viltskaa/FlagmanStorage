package com.example.flagmanstorage.QrScanner

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanIntentResult

import com.journeyapps.barcodescanner.ScanOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QrScanner(
    private val activity: Activity,
    private val scanLauncher: ActivityResultLauncher<ScanOptions>,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) {

    fun checkCameraPermission(onPermissionGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else if (activity.shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            Toast.makeText(activity, "Необходимо разрешение для использования камеры", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    fun showCamera() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.CODE_128)
            setPrompt("Сканируйте QR-код")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }

    fun handleScanResult(result: ScanIntentResult, onScanSuccessful: (String) -> Unit) {
        if (result.contents == null) {
            Toast.makeText(activity, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            onScanSuccessful(result.contents)
        }
    }
}

