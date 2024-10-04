package com.example.flagmanstorage

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityMain2Binding
import com.example.flagmanstorage.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var qrScanner: QrScanner
    private lateinit var userPreferences: UserPreferences
    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        qrScanner.handleQrScanResult(result, { scannedCode ->
            setResult(scannedCode)
        }, {
            // Обработка ошибки при разборе JSON
            Toast.makeText(this, "Ошибка при разборе данных QR-кода", Toast.LENGTH_SHORT).show()
        })
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            qrScanner.showCameraForQrOnly()
        } else {
            Toast.makeText(this, "Требуется разрешение на использование камеры", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViews()

        // Создаем экземпляр QrScanner
        qrScanner = QrScanner(this, scanLauncher, requestPermissionLauncher)
    }

    private fun initViews() {
        binding.buttonAuth.setOnClickListener {
            qrScanner.checkCameraPermission { qrScanner.showCamera() }
        }
    }

    private fun setResult(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            // Выводим значение "name" из QR-кода в Toast
            Toast.makeText(this, scannedCode, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBinding() {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        // Проверка авторизации через UserPreferences
        userPreferences = UserPreferences(this)
        val isLoggedIn = userPreferences.isLoggedIn() // Предполагается, что вы добавили этот метод в UserPreferences

        if (isLoggedIn) {
            // Если пользователь авторизован, перенаправляем его на другую страницу
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Закрываем текущую активность
        }
    }
}