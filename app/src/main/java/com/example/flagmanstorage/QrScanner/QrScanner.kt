package com.example.flagmanstorage.QrScanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.flagmanstorage.MainActivity
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
            setDesiredBarcodeFormats(ScanOptions.EAN_13)
            setPrompt("Сканируйте QR-код")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }
    fun showCameraForQrOnly() {
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
        if (result.contents == null) {
            Toast.makeText(activity, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            onScanSuccessful(result.contents)
        }
    }

    fun handleQrScanResult(result: ScanIntentResult, onJsonParsed: (String) -> Unit, onError: () -> Unit) {
        if (result.contents == null) {
            Toast.makeText(activity, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            try {
                // Предполагается, что результат содержимого QR-кода — это JSON строка
                val jsonObject = JSONObject(result.contents)
                val name = jsonObject.getString("name")
                onJsonParsed(name) // Возвращаем значение "name"
                UserPreferences(activity).saveUserName(name)
                UserPreferences(activity).saveLoginStatus(true)
                val intent = Intent(activity, MainActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            } catch (e: JSONException) {
                onError() // Возвращаем ошибку в случае некорректного JSON
                Toast.makeText(activity, "Ошибка при разборе данных QR-кода", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


