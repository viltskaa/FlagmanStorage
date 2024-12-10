package com.example.flagmanstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWB
import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWBAdapter
import com.example.flagmanstorage.databinding.ActivityShipmentsProdsBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShipmentsProds : AppCompatActivity() {

    private lateinit var binding: ActivityShipmentsProdsBinding // Замените на соответствующий класс привязки
    private lateinit var qrScanner: QrScanner
    private lateinit var itemAdapter: ItemFromWBAdapter
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
        // Инициализация qrScanner после инициализации binding
        qrScanner = QrScanner(this, scanLauncher, requestPermissionLauncher)

        initViews() // Инициализация кнопок и других элементов интерфейса

        // Инициализация RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.productList) // Замените на ID вашего RecyclerView
        itemAdapter = ItemFromWBAdapter(mutableListOf())
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchItemsFromServer("true") // Загрузка данных с сервера
    }

    private fun initViews() {
        binding.buttonScan.setOnClickListener {
            qrScanner.checkCameraPermission { qrScanner.showCamera() }
        }
    }

    private fun processScannedCode(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            // Логируем сканированный код
            Log.d("ShipmentsProds", "Сканированный код: $scannedCode")

            // Отправка кода на сервер
            sendScannedCodeToServer(scannedCode)
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchItemsFromServer(load: String) {
        val apiService = ApiClient.getClient().create(APIService::class.java)
        val call = apiService.getItems(load)

        call.enqueue(object : Callback<List<ItemFromWB>> {
            override fun onResponse(call: Call<List<ItemFromWB>>, response: Response<List<ItemFromWB>>) {
                if (response.isSuccessful) {
                    // Получаем список элементов
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()

                    // Обновляем адаптер с полученными данными
                    itemAdapter.updateItems(itemsFromServer)
                } else {
                    // Обработка ошибок
                    Toast.makeText(this@ShipmentsProds, "Не удалось получить данные", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ItemFromWB>>, t: Throwable) {
                // Обработка ошибки сети
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun sendScannedCodeToServer(article: String) {
        val apiService = ApiClient.getClient().create(APIService::class.java)
        val call = apiService.updateByArticle(article)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ShipmentsProds, "Код успешно отправлен на сервер", Toast.LENGTH_SHORT).show()
                    // Загружаем обновленный список элементов с сервера только после успешного обновления
                    fetchItemsFromServer("false")
                } else {
                    Toast.makeText(this@ShipmentsProds, "Ошибка отправки кода: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ShipmentsProds, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun initBinding() {
        binding = ActivityShipmentsProdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Вы точно хотите выйти? Данные не сохранятся.")
            .setPositiveButton("Да") { dialog, _ ->
                super.onBackPressed()
                dialog.dismiss()
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}