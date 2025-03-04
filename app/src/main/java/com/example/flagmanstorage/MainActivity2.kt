package com.example.flagmanstorage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
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
    private lateinit var userPreferences: UserPreferences
    private var buffer: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        binding.root.isFocusable = true
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        super.setCallbackAfterScan(::handleScanResult)
    }


    private fun handleScanResult(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            parseAndSaveUserData(scannedCode)
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseAndSaveUserData(scannedCode: String) {
        try {
            val qrcodeData = scannedCode.split(",")

            val surname = qrcodeData[1].substring(7)
            val name = qrcodeData[0].substring(4)
            val patronymic = qrcodeData[2].substring(10)

            val loginRequest = LoginRequest(scannedCode)


            val apiService = ApiClient.getClient(this).create(APIService::class.java)

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
                        Toast.makeText(this@MainActivity2, "Ошибка входа", Toast.LENGTH_SHORT).show()
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
