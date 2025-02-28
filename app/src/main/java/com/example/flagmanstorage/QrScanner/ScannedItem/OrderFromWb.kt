package com.example.flagmanstorage.QrScanner.ScannedItem

data class OrderFromWb (
    val orderUid: String,
    val items: List<ItemFromWB>
)