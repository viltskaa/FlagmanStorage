package com.example.flagmanstorage.API

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object ApiClient {
    //private const val BASE_URL = "http://10.0.0.250:8080"    // URL вашего сервера
    private const val BASE_URL = "http://192.168.172.52:8080"    // URL вашего сервера

    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}