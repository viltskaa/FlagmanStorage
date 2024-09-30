package com.example.flagmanstorage.API

import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {

    @POST("/product_introduction")
    fun sendScannedCode(@Body scannedCode: String): Call<Void>

    @POST("/product_shipment")
    fun sendListCodeTime(@Body scannedItems: List<ScannedItem>): Call<Void>
}