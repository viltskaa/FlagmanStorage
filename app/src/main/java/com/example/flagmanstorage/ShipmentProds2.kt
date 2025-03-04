package com.example.flagmanstorage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.API.Product
import com.example.flagmanstorage.databinding.ActivityIntroductionProdsBinding
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItemDisplayAdapter
import com.example.flagmanstorage.QrScanner.User.LoginResponse
import com.example.flagmanstorage.QrScanner.User.RefreshRequest
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShipmentProds2 : AppCompatActivity() {
    private lateinit var binding: ActivityIntroductionProdsBinding
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var adapter: ScannedItemDisplayAdapter
    private lateinit var userPreferences: UserPreferences
    private var isTorchOn = false

    private var buffer: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        userPreferences = UserPreferences(this)

        if (!userPreferences.isLoggedIn()) {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }


        initViews()
        preferencesHelper = PreferencesHelper(this)


        Thread {
            val scannedItems = preferencesHelper.getGroupedScannedItems()
            runOnUiThread {
                adapter = ScannedItemDisplayAdapter(scannedItems, preferencesHelper)
                binding.productList.adapter = adapter
                binding.productList.layoutManager = LinearLayoutManager(this)
            }
        }.start()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            return super.onKeyUp(keyCode, event)
        }

        return if (keyCode == KeyEvent.KEYCODE_ENTER) {
            processScannedCode(buffer);
            buffer = "";
            true
        } else {
            event?.let {
                buffer += it.unicodeChar.toChar();
            }

            super.onKeyUp(keyCode, event)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun processScannedCode(scannedCode: String) {
        if (scannedCode.isNotEmpty()
            && scannedCode.contains(",")
            && scannedCode.contains("gtin")
            && scannedCode.contains("time")) {
            val code = scannedCode.splitToSequence(",").first()
            val timestamp = scannedCode.splitToSequence(",")
                .last().drop(4).toLong() * 1000
            val scannedItem = ScannedItem(
                code.drop(4),
                timestamp,
                scannedCode
            )
            if (!preferencesHelper.isScannedItemExists(timestamp)) {
                preferencesHelper.saveScannedItem(scannedItem)
                adapter.notifyDataSetChanged()
                updateProductList()
            }
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        binding.buttonSend.setOnClickListener {
            val products = preferencesHelper.getScannedItems()
            if (products.isNotEmpty()) {
                val apiService = ApiClient.getClient(this).create(APIService::class.java)
                val call = apiService.sendShipment(products.map { Product(it.qrcode) })

                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@ShipmentProds2,
                                "Код успешно отправлен на сервер",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (response.code() == 401) {
                                handleUnauthorizedError()
                            }else{
                                Toast.makeText(
                                    this@ShipmentProds2,
                                    "Ошибка отправки кода",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(
                            this@ShipmentProds2,
                            "Ошибка сети: ${t.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
            } else {
                Toast.makeText(this, "Список пуст, заполните его", Toast.LENGTH_SHORT).show()
            }
            preferencesHelper.clearAllScannedItems()
            updateProductList()
        }
    }

    private fun updateProductList() {
        val products =
            preferencesHelper.getGroupedScannedItems() // Получаем обновленный список продуктов
        val adapter =
            ScannedItemDisplayAdapter(products, preferencesHelper) // Создаем новый адаптер
        binding.productList.adapter = adapter // Устанавливаем адаптер в RecyclerView
    }

    private fun initBinding() {
        binding = ActivityIntroductionProdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                        Toast.makeText(this@ShipmentProds2, "Ошибка при получении токена", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(this@ShipmentProds2, loginResponse?.msg.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ShipmentProds2, "Ошибка входа", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@ShipmentProds2, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}