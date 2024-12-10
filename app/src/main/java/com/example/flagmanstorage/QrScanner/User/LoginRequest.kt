package com.example.flagmanstorage.QrScanner.User

data class LoginRequest(
    val name: String,
    val surname: String,
    val patronymic: String
)