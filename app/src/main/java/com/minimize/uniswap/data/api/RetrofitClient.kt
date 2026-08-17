package com.minimize.uniswap.data.api

import com.minimize.uniswap.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
object RetrofitClient {
    // 10.0.2.2 is the localhost alias for the Android Emulator
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Order matters! Scalars handles plain text, Gson handles JSON.
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Your existing API service for items/feed
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Your NEW API service for Login/Signup
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}
