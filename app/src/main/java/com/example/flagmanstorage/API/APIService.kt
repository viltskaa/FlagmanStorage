package com.example.flagmanstorage.API

import com.example.flagmanstorage.QrScanner.ScannedItem.ApiResponse
import com.example.flagmanstorage.QrScanner.ScannedItem.OrderFromWb
import com.example.flagmanstorage.QrScanner.User.LoginRequest
import com.example.flagmanstorage.QrScanner.User.LoginResponse
import com.example.flagmanstorage.QrScanner.User.RefreshRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface APIService {
    @POST("/v1/item/product")
    fun sendProducts(@Body scannedItems: List<Product>): Call<Void>

    @POST("/v1/item/write_off")
    fun sendWriteOff(@Body scannedItems: List<Product>): Call<Void>

    @POST("/v1/item/shipment")
    fun sendShipment(@Body scannedItems: List<Product>): Call<Void>

    @POST("/v1/shipment_item/scanQrOnPalet")
    fun scanQrOnPalet(@Body item: ShipRequest): Call<ApiResponse>


    @POST("/v1/item/unique")
    fun checkUnique(@Body uniqueItem: UniqueItem): Call<UniqueDataAnswer>

    @GET("/v1/shipment_item")
    fun getItems(): Call<List<OrderFromWb>>

    @GET("/v1/shipment_item/toShipment")
    fun getItemsToShip(): Call<List<OrderFromWb>>

    @GET("/v1/shipment_item/checkShipmentItems")
    fun checkShipmentItems(): Call<ShipmentItemsStatusResponse>

    @GET("/v1/shipment_item/checkShipmentItemsToShip")
    fun checkShipmentItemsToShip(): Call<ShipmentItemsStatusResponse>
    @POST("/v1/shipment_item/to_ship")
    fun to_ship(): Call<Void>
    @POST("/v1/shipment_item/shipmentAll")
    fun shipmentAll(): Call<Void>
    @POST("/v1/shipment_item/scanQr")
    fun scanQrShip(@Body item: ShipRequest): Call<Void>

    @POST("/v1/shipment_item/scanQrToShip")
    fun scanQrToShip(@Body item: ShipRequest): Call<Void>

    @POST("/v1/shipment_item/outOfStock")

    fun outOfStock(@Body stockRequest: StockRequest): Call<StockResponse>
    @POST("/v1/auth/refresh")
    fun refresh(@Body loginRequest: RefreshRequest): Call<LoginResponse>

    @POST("/v1/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("/v1/item/check_storage")
    fun checkStorage(@Body product: Product): Call<CheckResponse>

    @POST("/v1/item/check_write_off")
    fun checkWriteOff(@Body product: Product): Call<CheckResponse>

    @POST("/v1/item/check_refund")
    fun checkRefund(@Body product: Product): Call<CheckResponse>

    @POST("/v1/item/refund")
    fun sendRefund(@Body scannedItems: List<Product>): Call<Void>

    @POST("/v1/shipment_item/cancel/{orderUid}")
    fun cancel(
        @Path("orderUid") orderUid: String,
        @Body requestBody: Map<String, Boolean>
    ): Call<Void>


}