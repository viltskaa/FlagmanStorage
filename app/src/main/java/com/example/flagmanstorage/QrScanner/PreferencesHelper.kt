package com.example.flagmanstorage.QrScanner

import android.content.Context
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem

class PreferencesHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("ScannedItems", Context.MODE_PRIVATE)

    fun saveScannedItem(item: ScannedItem) {
        val editor = sharedPreferences.edit()
        // Используем уникальный идентификатор для ключа, чтобы избежать коллизий
        val timestampKey = System.currentTimeMillis()
        editor.putString("code_$timestampKey", item.code)
        editor.putLong("timestamp_$timestampKey", item.timestamp)
        editor.apply()
    }

    fun getScannedItems(): List<ScannedItem> {
        val items = mutableListOf<ScannedItem>()
        val allEntries = sharedPreferences.all

        // Итерируемся по всем записям в SharedPreferences
        for ((key, value) in allEntries) {
            if (key.startsWith("code_") && value is String) {
                val timestampKey = key.replace("code_", "timestamp_")
                // Извлекаем временную метку, если она существует
                val timestamp = allEntries[timestampKey] as? Long ?: continue
                items.add(ScannedItem(value, timestamp))
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
            // Удаляем только те ключи, которые начинаются с "code_" или "timestamp_"
            if (key.startsWith("code_") || key.startsWith("timestamp_")) {
                editor.remove(key)
            }
        }
        editor.apply()  // Применяем изменения
    }

    fun isScannedItemExists(scannedCode: String): Boolean {
        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            if (key.startsWith("code_") && value == scannedCode) {
                return true // Если код уже существует
            }
        }
        return false // Код не найден
    }
}