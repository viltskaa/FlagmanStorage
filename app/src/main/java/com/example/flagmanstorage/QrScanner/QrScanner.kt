package com.example.flagmanstorage.QrScanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.MainActivity
import com.example.flagmanstorage.QrScanner.User.LoginRequest
import com.example.flagmanstorage.QrScanner.User.LoginResponse
import com.journeyapps.barcodescanner.ScanIntentResult

import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QrScanner(
    private val activity: Activity,
    private val scanLauncher: ActivityResultLauncher<ScanOptions>,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) {
    private var scanStartTime:Long = 0
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
        scanStartTime = System.currentTimeMillis()
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Сканируйте QR-код")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setTorchEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }
    fun showCameraForQrOnly() {
        scanStartTime = System.currentTimeMillis()
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE) // Устанавливаем только формат QR-кодов
            setPrompt("Сканируйте QR-код")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }

    fun handleScanResult(result: ScanIntentResult, onScanSuccessful: (String) -> Unit) {
        val scanEndTime = System.currentTimeMillis()
        if (result.contents == null) {
            Toast.makeText(activity, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            val time = (scanEndTime-scanStartTime)/1000.0
            Toast.makeText(activity, "Отсканировано за $time сек.", Toast.LENGTH_SHORT).show()
            onScanSuccessful(result.contents)
        }
    }

    fun handleQrScanResult(
        result: ScanIntentResult,
        onJsonParsed: (String) -> Unit,
        onError: () -> Unit
    ) {
        val scanEndTime = System.currentTimeMillis()
        if (result.contents == null) {
            Toast.makeText(activity, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            val time = (scanEndTime-scanStartTime)/1000.0
            Toast.makeText(activity, "Отсканировано за $time сек.", Toast.LENGTH_SHORT).show()
            onJsonParsed(result.contents)
        }
    }


}


