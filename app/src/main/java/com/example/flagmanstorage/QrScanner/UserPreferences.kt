package com.example.flagmanstorage.QrScanner

import android.content.Context
import android.content.SharedPreferences


class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    fun clearUserName() {
        prefs.edit().remove("user_name").apply()
    }

    fun saveLoginStatus(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    // Выход из аккаунта
    fun logout() {
        clearUserName()
        saveLoginStatus(false)
    }
}