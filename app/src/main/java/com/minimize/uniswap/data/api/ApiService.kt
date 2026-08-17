package com.minimize.uniswap.network

import com.minimize.uniswap.data.model.CampusItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    // Must return List<CampusItem> to match NetworkItemRepository
    @GET("api/items")
    suspend fun getItems(
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): List<CampusItem>

    // Must take CampusItem as a @Body and return a CampusItem
    @POST("api/items")
    suspend fun addItem(
        @Body item: CampusItem,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): CampusItem
}
