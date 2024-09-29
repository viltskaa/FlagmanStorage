package com.example.flagmanstorage.API

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {

    @POST("/your-endpoint")
    fun sendScannedCode(@Body scannedCode: String): Call<Void>
}