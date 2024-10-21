package com.example.flagmanstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.API.APIService
import com.example.flagmanstorage.API.ApiClient
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWB
import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWBAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShipmentsProds : AppCompatActivity() {

    private lateinit var preferencesHelper: PreferencesHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipments_prods)
        preferencesHelper = PreferencesHelper(this)

        fetchItemsFromServer()
    }
    private fun fetchItemsFromServer() {
        val apiService = ApiClient.getClient().create(APIService::class.java)
        val call = apiService.getItems()

        call.enqueue(object : Callback<List<ItemFromWB>> {
            override fun onResponse(call: Call<List<ItemFromWB>>, response: Response<List<ItemFromWB>>) {
                if (response.isSuccessful) {
                    // Получаем список элементов
                    val itemsFromServer = response.body()?.toMutableList() ?: mutableListOf()

                    // Сохраняем элементы в кэш
                    preferencesHelper.saveItemsFromWB(itemsFromServer)

                    // Теперь данные можно отобразить из кэша
                    displayItemsFromCache()
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


    private fun displayItemsFromCache() {
        val cachedItems = preferencesHelper.getItemsFromWB()

        if (cachedItems.isNotEmpty()) {
            // Найдите RecyclerView в макете
            val recyclerView: RecyclerView = findViewById(R.id.productList)
            val adapter = ItemFromWBAdapter(cachedItems, preferencesHelper)  // Адаптер для отображения элементов

            // Присвойте адаптер и установите LayoutManager
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)  // LinearLayoutManager или GridLayoutManager
        } else {
            Toast.makeText(this, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
        }
    }
}