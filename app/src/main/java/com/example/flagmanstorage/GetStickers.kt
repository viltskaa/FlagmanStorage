package com.example.flagmanstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.API.ShipRequest
import com.example.flagmanstorage.QrScanner.ScannedItem.ApiResponse
import com.example.flagmanstorage.QrScanner.User.LoginResponse
import com.example.flagmanstorage.QrScanner.User.RefreshRequest
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.utils.TwoDimScannerActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetStickers : TwoDimScannerActivity() {
    private lateinit var userPreferences: UserPreferences
    private var buffer: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_stickers)
        userPreferences = UserPreferences(this)
        super.setCallbackAfterScan(::handleScanResult)
    }
    private fun handleScanResult(scannedCode: String) {
            if (scannedCode.isNotEmpty()) {
                val newRequest = ShipRequest(scannedCode)
                sendScannedCodeToServer(newRequest)
            } else {
                Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendScannedCodeToServer(shipRequest: ShipRequest) {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)

        val call = apiService.scanQrOnPalet(shipRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message.toString() ?: "Успех, но нет сообщения"
                    Toast.makeText(this@GetStickers, message, Toast.LENGTH_SHORT).show()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        val errorMessage = response.body()?.message.toString() ?: "Неизвестная ошибка"
                        Toast.makeText(this@GetStickers, errorMessage, Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@GetStickers, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun handleUnauthorizedError() {
        val full_name:String = userPreferences.getUserName().toString()
        val values = full_name.split(" ")
        val name = values[1]
        val surname = values[0]
        val patronymic = values[2]
        val loginRequest = RefreshRequest(name, surname, patronymic)
        val apiService = ApiClient.getClient(this).create(APIService::class.java)
        val call = apiService.refresh(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    loginResponse?.token?.let {
                        userPreferences.saveToken(it)
                        userPreferences.saveLoginStatus(true)
                    } ?: run {
                        Toast.makeText(this@GetStickers, "Ошибка при получении токена", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(this@GetStickers, loginResponse?.msg.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@GetStickers, "Ошибка входа.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@GetStickers, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}