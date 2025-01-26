package com.example.flagmanstorage

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.User.LoginRequest
import com.example.flagmanstorage.QrScanner.User.LoginResponse
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityMain2Binding
import com.example.flagmanstorage.utils.TwoDimScannerActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity2 : TwoDimScannerActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var qrScanner: QrScanner
    private lateinit var userPreferences: UserPreferences
    private var buffer: String = ""

    // Регистрация для обработки результата сканирования
    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        qrScanner.handleQrScanResult(result,
            { scannedCode ->
                handleScanResult(scannedCode)
            },
            {
                Toast.makeText(this, "Ошибка при разборе данных QR-кода", Toast.LENGTH_SHORT).show()
            })
    }

    // Регистрация для запроса разрешений на камеру
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            qrScanner.showCameraForQrOnly() // Если разрешение получено, запускаем сканирование
        } else {
            Toast.makeText(this, "Требуется разрешение на использование камеры", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViews()

        // Инициализация экземпляра QrScanner
        qrScanner = QrScanner(this, scanLauncher, requestPermissionLauncher)
        super.setCallbackAfterScan(::handleScanResult)
    }

    private fun initViews() {
        binding.buttonAuth.setOnClickListener {
            handleScanResult("test,test,test")
        }
    }

    // Обработка результата сканирования
    private fun handleScanResult(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            // Преобразуем данные QR в объект и сохраняем в SharedPreferences
            parseAndSaveUserData(scannedCode)
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseAndSaveUserData(scannedCode: String) {
        try {
            val values = scannedCode.split(",")
            if (values.count() != 3) {
                Toast.makeText(this, "Ошибка при разборе QR-кода", Toast.LENGTH_SHORT).show()
                return
            }

            val name = values[0]
            val surname = values[1]
            val patronymic = values[2]

            val loginRequest = LoginRequest(name, surname, patronymic)

            val apiService = ApiClient.getClient().create(APIService::class.java)

            apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        loginResponse?.token?.let {
                            userPreferences = UserPreferences(this@MainActivity2)
                            userPreferences.saveUserName("$surname $name $patronymic")
                            userPreferences.saveToken(it)
                            userPreferences.saveLoginStatus(true)

                            // Переходим на главную страницу
                            startActivity(Intent(this@MainActivity2, MainActivity::class.java))
                            finish()
                        } ?: run {
                            Toast.makeText(this@MainActivity2, "Ошибка при получении токена", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity2, "Ошибка входа: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity2, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: JSONException) {
            Toast.makeText(this, "Ошибка при разборе QR-кода", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBinding() {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        // Проверка состояния авторизации
        userPreferences = UserPreferences(this)
        if (userPreferences.isLoggedIn()) {
            // Перенаправляем пользователя, если он уже авторизован
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
