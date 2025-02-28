package com.example.flagmanstorage.QrScanner.ScannedItem

data class ItemFromWB(
    val id: Int,
    val article: String,
    val status: String,
    val scanned: String,
    val for_this: String,
)