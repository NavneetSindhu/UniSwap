package com.example.uniswap.data.network

import com.example.uniswap.data.model.LoginRequest
import com.example.uniswap.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthApiService {
    // Signup remains POST because it creates a new resource
    @POST("api/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<String>

    // Updated to GET to match your Spring Boot service
    // Retrofit uses @Query to attach parameters to the URL
    @POST("api/auth/login") // Matches the Spring change
    suspend fun login(@Body request: LoginRequest): Response<String>
}