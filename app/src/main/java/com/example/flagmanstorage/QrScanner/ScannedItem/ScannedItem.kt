package com.example.flagmanstorage.QrScanner.ScannedItem

data class ScannedItem(
    val code: String,
    val timestamp: Long,
    val positionX: Float,
    val positionY: Float,
    val positionZ: Float
)
