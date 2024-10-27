package com.example.flagmanstorage.API

import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWB
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {


    @POST("/product_introduction")
    fun sendListCodeTime(@Body scannedItems: List<ScannedItem>): Call<Void>

    @GET("/orders")
    fun getItems(@Query("load") bool: String): Call<List<ItemFromWB>>

    @POST("/orders/updateByArticle")
    fun updateByArticle(@Body article:String):Call<Void>
}