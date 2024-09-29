package com.example.flagmanstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItemAdapter
import com.example.flagmanstorage.databinding.ActivityMainBinding
import com.example.flagmanstorage.databinding.ActivityShipmentProdsBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult

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
                adapter = ScannedItemAdapter(scannedItems)
                binding.productList.adapter = adapter
                binding.productList.layoutManager = LinearLayoutManager(this)
            }
        }.start()
        // Загрузка данных из кэша

        // Инициализация адаптера и привязка к RecyclerView

    }

    private fun processScannedCode(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            val scannedItem = ScannedItem(scannedCode, System.currentTimeMillis())
            preferencesHelper.saveScannedItem(scannedItem)
            // Добавляем новый элемент в список
            adapter.notifyDataSetChanged()  // Сообщаем адаптеру об изменениях
            updateProductList()
            Toast.makeText(this, "Сканированный код: ${scannedCode}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Сканированный код пустой", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        binding.buttonAdd.setOnClickListener {
            qrScanner.checkCameraPermission { qrScanner.showCamera() }
        }
        binding.buttonSend.setOnClickListener{
            preferencesHelper.clearAllScannedItems()
            updateProductList()
        }
    }

    private fun updateProductList() {
        val products = preferencesHelper.getScannedItems() // Получаем обновленный список продуктов
        val adapter = ScannedItemAdapter(products) // Создаем новый адаптер
        binding.productList.adapter = adapter // Устанавливаем адаптер в RecyclerView
    }
    private fun initBinding() {
        binding = ActivityShipmentProdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}