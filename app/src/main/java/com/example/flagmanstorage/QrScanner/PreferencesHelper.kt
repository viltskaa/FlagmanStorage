package com.example.flagmanstorage.QrScanner

import android.content.Context
import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWB
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("ScannedItems", Context.MODE_PRIVATE)
    private val userPreferences = UserPreferences(context)

    private fun getUserKeyPrefix(): String {
        val userName = userPreferences.getUserName() ?: return ""
        return "$userName/"
    }

    fun saveScannedItem(item: ScannedItem) {
        val editor = sharedPreferences.edit()
        val uniqueKey = "${item.code}_${item.timestamp}" // Создаем уникальный ключ на основе штрихкода и временной метки
        editor.putString("${getUserKeyPrefix()}code_$uniqueKey", item.code)
        editor.putLong("${getUserKeyPrefix()}timestamp_$uniqueKey", item.timestamp)
        // Можно добавить сохранение данных о положении устройства
        editor.putFloat("${getUserKeyPrefix()}posX_$uniqueKey", item.positionX)
        editor.putFloat("${getUserKeyPrefix()}posY_$uniqueKey", item.positionY)
        editor.putFloat("${getUserKeyPrefix()}posZ_$uniqueKey", item.positionZ)
        editor.apply()
    }


    fun removeScannedItem(item: ScannedItem) {
        val uniqueKey = "${item.code}_${item.timestamp}"
        val editor = sharedPreferences.edit()
        editor.remove("${getUserKeyPrefix()}code_$uniqueKey")
        editor.remove("${getUserKeyPrefix()}timestamp_$uniqueKey")
        // Удаляем данные о положении устройства
        editor.remove("${getUserKeyPrefix()}posX_$uniqueKey")
        editor.remove("${getUserKeyPrefix()}posY_$uniqueKey")
        editor.remove("${getUserKeyPrefix()}posZ_$uniqueKey")
        editor.apply()
    }

    fun getScannedItems(): MutableList<ScannedItem> {
        val items = mutableListOf<ScannedItem>()
        val allEntries = sharedPreferences.all

        // Итерируемся по всем записям в SharedPreferences
        for ((key, value) in allEntries) {
            if (key.startsWith("${getUserKeyPrefix()}code_") && value is String) {
                val uniqueKey = key.replace("${getUserKeyPrefix()}code_", "")
                val timestamp = sharedPreferences.getLong("${getUserKeyPrefix()}timestamp_$uniqueKey", 0L)
                val positionX = sharedPreferences.getFloat("${getUserKeyPrefix()}posX_$uniqueKey", 0f)
                val positionY = sharedPreferences.getFloat("${getUserKeyPrefix()}posY_$uniqueKey", 0f)
                val positionZ = sharedPreferences.getFloat("${getUserKeyPrefix()}posZ_$uniqueKey", 0f)
                if (timestamp != 0L) {
                    items.add(ScannedItem(value, timestamp, positionX, positionY, positionZ))
                }
            }
        }
        items.sortByDescending { it.timestamp }
        return items
    }

    fun clearAllScannedItems() {
        val editor = sharedPreferences.edit()
        val allEntries = sharedPreferences.all

        // Итерируемся по всем записям в SharedPreferences
        for ((key, _) in allEntries) {
            if (key.startsWith(getUserKeyPrefix())) {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    fun isScannedItemExists(scannedCode: String, positionX: Float, positionY: Float, positionZ: Float): Boolean {
        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            if (key.startsWith("${getUserKeyPrefix()}code_") && value == scannedCode) {
                val uniqueKey = key.replace("${getUserKeyPrefix()}code_", "")
                val savedX = sharedPreferences.getFloat("${getUserKeyPrefix()}posX_$uniqueKey", 0f)
                val savedY = sharedPreferences.getFloat("${getUserKeyPrefix()}posY_$uniqueKey", 0f)
                val savedZ = sharedPreferences.getFloat("${getUserKeyPrefix()}posZ_$uniqueKey", 0f)
                if (isPositionSimilar(savedX, savedY, savedZ, positionX, positionY, positionZ)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isPositionSimilar(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Boolean {
        val tolerance = 1.5f
        return Math.abs(x1 - x2) < tolerance &&
                Math.abs(y1 - y2) < tolerance
    }

    // Метод для сохранения списка заказов Wildberries в кэш
    fun saveItemsFromWB(items: MutableList<ItemFromWB>) {
        val editor = sharedPreferences.edit()

        // Преобразуем список объектов ArticleCount в JSON-строку
        val gson = Gson()
        val jsonItems = gson.toJson(items)

        // Сохраняем JSON-строку в SharedPreferences
        editor.putString("${getUserKeyPrefix()}wb_items", jsonItems)
        editor.apply()
    }

    // Метод для извлечения списка объектов ArticleCount из кэша
    fun getItemsFromWB(): MutableList<ItemFromWB> {
        // Получаем JSON-строку из SharedPreferences
        val jsonItems = sharedPreferences.getString("${getUserKeyPrefix()}wb_items", null) ?: return mutableListOf()

        // Преобразуем JSON-строку обратно в список объектов ArticleCount
        val gson = Gson()
        val type = object : TypeToken<MutableList<ItemFromWB>>() {}.type

        return gson.fromJson(jsonItems, type)
    }
}
