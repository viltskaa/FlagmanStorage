package com.example.flagmanstorage
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityMainBinding

import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var qrScanner: QrScanner
    private lateinit var userPreferences: UserPreferences

    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        qrScanner.handleScanResult(result) { scannedCode ->
            setResult(scannedCode)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            qrScanner.showCamera()
        } else {
            Toast.makeText(this, "Требуется разрешение на использование камеры", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(this)
        if (!userPreferences.isLoggedIn()) {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }

        initBinding()
        initViews()

        // Создаем экземпляр QrScanner
        qrScanner = QrScanner(this, scanLauncher, requestPermissionLauncher)



    }

    private fun initViews() {
        binding.btnAddToWarehouse.setOnClickListener {
            qrScanner.checkCameraPermission { qrScanner.showCamera() }
        }
        binding.btnAddToShipping.setOnClickListener{
            val intent = Intent(this, ShipmentProds::class.java)
            startActivity(intent)
        }
    }

    private fun setResult(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            // Инициализируем Retrofit
            val apiService = ApiClient.getClient().create(APIService::class.java)
            val call = apiService.sendScannedCode(scannedCode)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Код успешно отправлен!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Response Code: ${response.code()}, Message: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Ошибка соединения: ${t.message}", Toast.LENGTH_LONG).show()
                    t.message?.let { Log.d("NADO", it) }
                }
            })
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userName = userPreferences.getUserName()
        if (userName != null) {
            binding.userInfo.text = "Авторизован: $userName"
        }

        // Кнопка выхода
        binding.btnLogout.setOnClickListener {
            userPreferences.clearUserName() // Очищаем данные пользователя
            userPreferences.logout()
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish() // Закрываем активность
        }
    }
}
