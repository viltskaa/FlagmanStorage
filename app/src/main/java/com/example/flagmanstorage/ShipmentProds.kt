package com.example.flagmanstorage

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItemAdapter
import com.example.flagmanstorage.databinding.ActivityShipmentProdsBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShipmentProds : AppCompatActivity() {

    private lateinit var binding: ActivityShipmentProdsBinding
    private lateinit var qrScanner: QrScanner
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var adapter: ScannedItemAdapter

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

        // Инициализация qrScanner после инициализации binding
        qrScanner = QrScanner(this, scanLauncher, requestPermissionLauncher)

        initViews() // Теперь инициализация views происходит после qrScanner
        preferencesHelper = PreferencesHelper(this)


        Thread {
            val scannedItems = preferencesHelper.getScannedItems()
            runOnUiThread {
                adapter = ScannedItemAdapter(scannedItems,preferencesHelper)
                binding.productList.adapter = adapter
                binding.productList.layoutManager = LinearLayoutManager(this)
            }
        }.start()
        // Загрузка данных из кэша

        // Инициализация адаптера и привязка к RecyclerView

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun processScannedCode(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            if (!preferencesHelper.isScannedItemExists(scannedCode)) { // Проверка наличия кода
                val scannedItem = ScannedItem(scannedCode, System.currentTimeMillis())
                preferencesHelper.saveScannedItem(scannedItem)
                adapter.notifyDataSetChanged()
                updateProductList()
            } else {
                Toast.makeText(this, "Этот код уже был сканирован", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        binding.buttonAdd.setOnClickListener {
            qrScanner.checkCameraPermission { qrScanner.showCamera() }
        }
        binding.buttonSend.setOnClickListener{
            val products = preferencesHelper.getScannedItems()
            if (products.size!=0) {
                // Инициализируем Retrofit
                val apiService = ApiClient.getClient().create(APIService::class.java)
                val call = apiService.sendListCodeTime(products)
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ShipmentProds, "Список успешно отправлен!", Toast.LENGTH_LONG).show()

                        } else {
                            Toast.makeText(this@ShipmentProds, "Response Code: ${response.code()}, Message: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ShipmentProds, "Ошибка соединения: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(this, "Список пуст заполните его", Toast.LENGTH_SHORT).show()
            }
            preferencesHelper.clearAllScannedItems()
            updateProductList()
        }
    }

    private fun updateProductList() {
        val products = preferencesHelper.getScannedItems() // Получаем обновленный список продуктов
        val adapter = ScannedItemAdapter(products,preferencesHelper) // Создаем новый адаптер
        binding.productList.adapter = adapter // Устанавливаем адаптер в RecyclerView
    }
    private fun initBinding() {
        binding = ActivityShipmentProdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}