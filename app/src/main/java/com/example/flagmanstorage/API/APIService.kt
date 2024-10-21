package com.example.flagmanstorage.API

import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWB
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {


    @POST("/product_introduction")
    fun sendListCodeTime(@Body scannedItems: List<ScannedItem>): Call<Void>

    @GET("/orders")
    fun getItems(): Call<List<ItemFromWB>>
}