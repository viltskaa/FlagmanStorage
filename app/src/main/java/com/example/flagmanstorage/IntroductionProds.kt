package com.example.flagmanstorage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.QrScanner
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItemAdapter
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItemDisplayAdapter
import com.example.flagmanstorage.QrScanner.UserPreferences
import com.example.flagmanstorage.databinding.ActivityIntroductionProdsBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IntroductionProds : AppCompatActivity() {

    private lateinit var binding: ActivityIntroductionProdsBinding
    private lateinit var qrScanner: QrScanner
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var adapter: ScannedItemDisplayAdapter
    private lateinit var userPreferences: UserPreferences
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var accelerometerValues = FloatArray(3) // x, y, z координаты
    private var isTorchOn = false
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

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues[0] = event.values[0] // Данные по оси X
                accelerometerValues[1] = event.values[1] // Данные по оси Y
                accelerometerValues[2] = event.values[2] // Данные по оси Z
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Не требуется
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun processScannedCode(scannedCode: String) {
        if (scannedCode.isNotEmpty()) {
            val scannedItem = ScannedItem(
                scannedCode,
                System.currentTimeMillis(),
                accelerometerValues[0],
                accelerometerValues[1],
                accelerometerValues[2]
            )
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val switch = prefs.getBoolean("switch",true)
            if (preferencesHelper.isScannedItemExists(scannedItem.code, scannedItem.positionX, scannedItem.positionY, scannedItem.positionZ) && switch) {
                // Создаем диалоговое окно для подтверждения
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Повторное добавление")
                builder.setMessage("Кажется, этот код уже был добавлен. Вы уверены, что хотите добавить его снова?")

                // Если пользователь выбрал "Да"
                builder.setPositiveButton("Да") { dialog, which ->
                    preferencesHelper.saveScannedItem(scannedItem)
                    adapter.notifyDataSetChanged()
                    updateProductList()
                }

                // Если пользователь выбрал "Нет"
                builder.setNegativeButton("Нет") { dialog, which ->
                    dialog.dismiss()
                }

                // Показываем диалог
                builder.show()
            } else {
                // Если элемент не существует, добавляем его без подтверждения
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
        binding.buttonOtchet.setOnClickListener {
            val apiService = ApiClient.getClient().create(APIService::class.java)
            apiService.report().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@IntroductionProds, "Список успешно отправлен!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@IntroductionProds, "Response Code: ${response.code()}, Message: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@IntroductionProds, "Ошибка соединения: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
        binding.buttonSend.setOnClickListener {
            val products = preferencesHelper.getScannedItems()
            if (products.isNotEmpty()) {
                // Инициализация Retrofit
                val apiService = ApiClient.getClient().create(APIService::class.java)
                val call = apiService.sendListCodeTime(products)
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@IntroductionProds, "Список успешно отправлен!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@IntroductionProds, "Response Code: ${response.code()}, Message: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@IntroductionProds, "Ошибка соединения: ${t.message}", Toast.LENGTH_LONG).show()
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
        val products = preferencesHelper.getGroupedScannedItems() // Получаем обновленный список продуктов
        val adapter = ScannedItemDisplayAdapter(products, preferencesHelper) // Создаем новый адаптер
        binding.productList.adapter = adapter // Устанавливаем адаптер в RecyclerView
    }

    private fun initBinding() {
        binding = ActivityIntroductionProdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}


