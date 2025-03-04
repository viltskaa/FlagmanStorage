package com.example.flagmanstorage

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
import com.example.flagmanstorage.API.StockRequest
import com.example.flagmanstorage.API.StockResponse
import com.example.flagmanstorage.QrScanner.ScannedItem.OrderFromWb
import com.example.flagmanstorage.QrScanner.ScannedItem.OrderFromWbAdapter
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
    private lateinit var itemAdapter: OrderFromWbAdapter
    private lateinit var buttonSuccess: Button
    private lateinit var userPreferences: UserPreferences
    private var currentOrderUid: String = ""
    private var state = true
    private var buffer: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        Log.d("ShipmentsProds", "Заход на страницу ShipmentsProds")
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
            handleCancelOrder(product)
        }
        itemAdapter.onOutOfStockClickListener = { product ->
            handleOutOfStockOrder(product)
        }
        fetchItemsFromServer()
    }

    private fun initViews() {
        binding.buttonShip.setOnClickListener {
            shipping()
        }
    }

    private fun handleScanResult(scannedCode: String) {
        if (state) {
            if (scannedCode.isNotEmpty()) {
                val newRequest = ShipRequest(scannedCode)
                sendScannedCodeToServer(newRequest)
            } else {
                Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "$scannedCode", Toast.LENGTH_SHORT).show()
            outOfStock(scannedCode, currentOrderUid)
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
                    checkShipmentItemsFromServer()
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
                        Toast.makeText(this@ShipmentsProds, "Ошибка при сканировании.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@ShipmentsProds, "Ошибка отгрузки.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun handleOutOfStockOrder(order: OrderFromWb) {
        state = false
        currentOrderUid = order.orderUid
        val alertDialog = AlertDialog.Builder(this)
            .setMessage("Чтобы перенести заказ отсканируйте qr бригадира")
            .setNegativeButton("Отмена") { dialog, which ->
                state = true
            }
            .create()

        alertDialog.setCancelable(false)
        alertDialog.setOnShowListener {
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.clearFocus()


            negativeButton.isFocusable = false
            negativeButton.isFocusableInTouchMode = false

            binding.root.requestFocus()
        }
        alertDialog.setOnKeyListener { _: DialogInterface, keyCode: Int, event: KeyEvent? ->
            if (event != null && event.action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    handleScanResult(buffer)
                    buffer = ""
                    state=true
                    alertDialog.dismiss()
                    return@setOnKeyListener true
                } else if (keyCode != KeyEvent.KEYCODE_BACK) {
                    buffer += event.unicodeChar.toChar()
                }
            }
            return@setOnKeyListener false
        }


        alertDialog.show()
    }

    private fun outOfStock(scannedCode: String,orderUid: String) {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)
        val qrcodeData = scannedCode.split(",")
        val surname = qrcodeData[1].substring(7)
        val name = qrcodeData[0].substring(4)
        val patronymic = qrcodeData[2].substring(10)
        val newRequest = StockRequest(orderUid,"$surname $name $patronymic")
        val call = apiService.outOfStock(newRequest)

        call.enqueue(object : Callback<StockResponse> {
            override fun onResponse(call: Call<StockResponse>, response: Response<StockResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ShipmentsProds, "Не достаток был перенесен", Toast.LENGTH_SHORT).show()
                    fetchItemsFromServer()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        Toast.makeText(this@ShipmentsProds, "Ошибка при одобрении.", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onFailure(call: Call<StockResponse>, t: Throwable) {
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
                        Toast.makeText(this@ShipmentsProds, "Ошибка проверки.", Toast.LENGTH_SHORT).show()
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


    private fun handleCancelOrder(order: OrderFromWb) {
        AlertDialog.Builder(this)
            .setMessage("Вы уверены, что хотите отменить заказ ${order.orderUid}?")
            .setPositiveButton("Да") { dialog, which ->
                cancelOrder(order)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun cancelOrder(order: OrderFromWb) {
        val apiService = ApiClient.getClient(this).create(APIService::class.java)

        val call = apiService.cancel(order.orderUid)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(this@ShipmentsProds, "Заказ ${order.orderUid} отменен", Toast.LENGTH_SHORT).show()
                    fetchItemsFromServer()
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    }else{
                        Toast.makeText(this@ShipmentsProds, "Ошибка отмены.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@ShipmentsProds, "Ошибка при получении токена", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(this@ShipmentsProds, loginResponse?.msg.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ShipmentsProds, "Ошибка входа.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}