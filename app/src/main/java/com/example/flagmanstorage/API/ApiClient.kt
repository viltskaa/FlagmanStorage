package com.example.flagmanstorage.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object ApiClient {
//    private const val BASE_URL = "http://10.0.0.250:8080"    // URL вашего сервера
    private const val BASE_URL = "http://192.168.0.100:8080"    // URL вашего сервера

    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}