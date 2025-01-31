package com.example.flagmanstorage.API

import com.example.flagmanstorage.QrScanner.ScannedItem.ItemFromWB
import com.example.flagmanstorage.QrScanner.ScannedItem.ScannedItem
import com.example.flagmanstorage.QrScanner.User.LoginRequest
import com.example.flagmanstorage.QrScanner.User.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @POST("/v1/item/product")
    fun sendProducts(@Body scannedItems: List<Product>): Call<Void>

    @POST("/v1/item/write_off")
    fun sendWriteOff(@Body scannedItems: List<Product>): Call<Void>

    @POST("/v1/item/shipment")
    fun sendShipment(@Body scannedItems: List<Product>): Call<Void>

    @POST("/v1/item/unique")
    fun checkUnique(@Body uniqueItem: UniqueItem): Call<UniqueDataAnswer>

    @GET("/v1/shipment_item")
    fun getItems(): Call<List<ItemFromWB>>

    @GET("/v1/item/report")
    fun report(): Call<ResponseBody>

    @POST("/v1/shipment_item/updateByArticle")
    fun updateByArticle(@Body item: UpdateRequest): Call<Void>

    @GET("/v1/shipment_item/checkShipmentItems")
    fun checkShipmentItems(): Call<ShipmentItemsStatusResponse>

    @POST("/v1/shipment_item/ship")
    fun ship(): Call<Void>

    @POST("/v1/shipment_item/scanQr")
    fun scanQrShip(@Body item: ShipRequest): Call<Void>

    @POST("/v1/shipment_item/{id}")

    fun outOfStock(@Path("id") itemId: Int): Call<Void>


    @POST("/v1/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}