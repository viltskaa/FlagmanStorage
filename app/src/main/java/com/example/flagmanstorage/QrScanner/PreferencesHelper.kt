package com.example.flagmanstorage.QrScanner

import android.content.Context
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem

class PreferencesHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("ScannedItems", Context.MODE_PRIVATE)
    private val userPreferences = UserPreferences(context)

    private fun getUserKeyPrefix(): String {
        val userName = userPreferences.getUserName() ?: return ""
        return "$userName/"
    }

    fun saveScannedItem(item: ScannedItem) {
        val editor = sharedPreferences.edit()
        // Используем уникальный идентификатор для ключа, чтобы избежать коллизий
        val timestampKey = item.timestamp
        editor.putString("${getUserKeyPrefix()}code_$timestampKey", item.code)
        editor.putLong("${getUserKeyPrefix()}timestamp_$timestampKey", item.timestamp)
        editor.apply()
    }

    fun removeScannedItem(item: ScannedItem) {
        val editor = sharedPreferences.edit()
        val timestampKey = item.timestamp
        editor.remove("${getUserKeyPrefix()}code_$timestampKey")
        editor.remove("${getUserKeyPrefix()}timestamp_$timestampKey")
        editor.apply()
    }

    fun getScannedItems(): MutableList<ScannedItem> {
        val items = mutableListOf<ScannedItem>()
        val allEntries = sharedPreferences.all

        // Итерируемся по всем записям в SharedPreferences
        for ((key, value) in allEntries) {
            if (key.startsWith("${getUserKeyPrefix()}code_") && value is String) {
                val timestampKey = key.replace("${getUserKeyPrefix()}code_", "${getUserKeyPrefix()}timestamp_")
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
            // Удаляем только те ключи, которые начинаются с имени пользователя
            if (key.startsWith(getUserKeyPrefix())) {
                editor.remove(key)
            }
        }
        editor.apply()  // Применяем изменения
    }

    fun isScannedItemExists(scannedCode: String): Boolean {
        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            if (key.startsWith("${getUserKeyPrefix()}code_") && value == scannedCode) {
                return true // Если код уже существует
            }
        }
        return false // Код не найден
    }
}
