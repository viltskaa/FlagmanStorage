package com.example.flagmanstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.API.ShipRequest
import com.example.flagmanstorage.API.ShipmentItemsStatusResponse
import com.example.flagmanstorage.API.UniqueItem
import com.example.flagmanstorage.API.UpdateRequest
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWB
import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWBAdapter
import com.example.flagmanstorage.QrScanner.ScannedItem.OrderFromWb
import com.example.flagmanstorage.QrScanner.ScannedItem.OrderFromWbAdapter
import com.example.flagmanstorage.QrScanner.User.LoginRequest
import com.example.flagmanstorage.QrScanner.User.LoginResponse
import com.example.flagmanstorage.QrScanner.User.RefreshRequest
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityShipmentsProdsBinding
import com.example.flagmanstorage.utils.TwoDimScannerActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShipmentsProds : TwoDimScannerActivity() {

    private lateinit var binding: ActivityShipmentsProdsBinding // Замените на соответствующий класс привязки
    private lateinit var qrScanner: QrScanner
    private lateinit var itemAdapter: OrderFromWbAdapter
    private lateinit var buttonSuccess: Button
    private lateinit var userPreferences: UserPreferences
    private var buffer: String = ""
    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        qrScanner.handleScanResult(result) { scannedCode ->
            processScannedCode(scannedCode)
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
        initBinding()
        Log.d("ShipmentsProds", "Заход на страницу ShipmentsProds")
        qrScanner = QrScanner(this, scanLauncher, requestPermissionLauncher)
        buttonSuccess = findViewById(R.id.button_ship)
        binding.root.isFocusable = true
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        userPreferences = UserPreferences(this)
        initViews()
        super.setCallbackAfterScan(::handleScanResult)
        val recyclerView = findViewById<RecyclerView>(R.id.productList)
        itemAdapter = OrderFromWbAdapter(mutableListOf())
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemAdapter.onActionClickListener = { product ->
            outOfStock(product.orderUid)
        }
        fetchItemsFromServer()
    }

    private fun initViews() {
        binding.buttonShip.setOnClickListener {
            shipping()
        }
    }

    private fun handleScanResult(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            val newRequest = ShipRequest(scannedCode)
            sendScannedCodeToServer(newRequest)
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }
    private fun processScannedCode(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            Log.d("ShipmentsProds", "Сканированный код: $scannedCode")
            val newRequest = ShipRequest(scannedCode)
            sendScannedCodeToServer(newRequest)
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchItemsFromServer() {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)
        val call = apiService.getItems()

        call.enqueue(object : Callback<List<OrderFromWb>> {
            override fun onResponse(call: Call<List<OrderFromWb>>, response: Response<List<OrderFromWb>>) {
                if (response.isSuccessful) {
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()

                    itemAdapter.updateItems(itemsFromServer)
                    //checkShipmentItemsFromServer()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        Toast.makeText(this@ShipmentsProds, "Не удалось получить данные", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onFailure(call: Call<List<OrderFromWb>>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun sendScannedCodeToServer(shipRequest: ShipRequest) {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)

        val call = apiService.scanQrShip(shipRequest)

       call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ShipmentsProds, "Успешно все", Toast.LENGTH_SHORT).show()
                    fetchItemsFromServer()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        Toast.makeText(this@ShipmentsProds, "Ошибка при сканировании: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun shipping() {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)

        val call = apiService.ship()

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ShipmentsProds, "Отгрузка успешна", Toast.LENGTH_SHORT).show()
                    fetchItemsFromServer()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        Toast.makeText(this@ShipmentsProds, "Ошибка отгрузки: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun outOfStock(id:String) {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)

        val call = apiService.outOfStock(id)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ShipmentsProds, "Не достаток был одобрен", Toast.LENGTH_SHORT).show()
                    fetchItemsFromServer()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        Toast.makeText(this@ShipmentsProds, "Ошибка при одобрении: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkShipmentItemsFromServer() {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)

        val call = apiService.checkShipmentItems()

        call.enqueue(object : Callback<ShipmentItemsStatusResponse> {
            override fun onResponse(call: Call<ShipmentItemsStatusResponse>, response: Response<ShipmentItemsStatusResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    buttonSuccess.isEnabled = result?.status == "true"
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        Toast.makeText(this@ShipmentsProds, "Ошибка проверки: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ShipmentItemsStatusResponse>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initBinding() {
        binding = ActivityShipmentsProdsBinding.inflate(layoutInflater)
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
                        Toast.makeText(this@ShipmentsProds, "Ошибка при получении токена", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(this@ShipmentsProds, loginResponse?.msg.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ShipmentsProds, "Ошибка входа: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}