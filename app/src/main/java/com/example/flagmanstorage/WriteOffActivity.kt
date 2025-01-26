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
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItemDisplayAdapter
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityIntroductionProdsBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WriteOffActivity: AppCompatActivity() {
    private lateinit var binding: ActivityIntroductionProdsBinding
    private lateinit var qrScanner: QrScanner
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var adapter: ScannedItemDisplayAdapter
    private lateinit var userPreferences: UserPreferences
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var accelerometerValues = FloatArray(3) // x, y, z координаты
    private var isTorchOn = false

    private var buffer: String = ""

    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            qrScanner.handleScanResult(result) { scannedCode ->
                processScannedCode(scannedCode)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                qrScanner.showCamera()
            } else {
                Toast.makeText(
                    this,
                    "Требуется разрешение на использование камеры",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        userPreferences = UserPreferences(this)

        if (!userPreferences.isLoggedIn()) {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }

        qrScanner = QrScanner(this, scanLauncher, requestPermissionLauncher)

        initViews()
        preferencesHelper = PreferencesHelper(this)

        // Инициализация SensorManager и акселерометра
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

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

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues[0] = event.values[0] // Данные по оси X
                accelerometerValues[1] = event.values[1] // Данные по оси Y
                accelerometerValues[2] = event.values[2] // Данные по оси Z
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
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
                accelerometerValues[0],
                accelerometerValues[1],
                accelerometerValues[2],
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
        binding.buttonAdd.setOnClickListener {
            qrScanner.checkCameraPermission { qrScanner.showCamera() }
        }
        binding.buttonSend.setOnClickListener {
            val products = preferencesHelper.getScannedItems()
            if (products.isNotEmpty()) {
                val apiService = ApiClient.getClient().create(APIService::class.java)
                val call = apiService.sendWriteOff(products.map { Product(it.qrcode) })

                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@WriteOffActivity,
                                "Код успешно отправлен на сервер",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@WriteOffActivity,
                                "Ошибка отправки кода: ${response.code()} ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(
                            this@WriteOffActivity,
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
        val products = preferencesHelper.getGroupedScannedItems()
        val adapter = ScannedItemDisplayAdapter(products, preferencesHelper)
        binding.productList.adapter = adapter
    }

    private fun initBinding() {
        binding = ActivityIntroductionProdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}