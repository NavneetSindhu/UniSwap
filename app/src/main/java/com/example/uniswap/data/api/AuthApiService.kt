package com.example.uniswap.data.api

import com.example.uniswap.data.model.AuthResponse
import com.example.uniswap.data.model.LoginRequest
import com.example.uniswap.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login") // This is correct
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup") // FIX: Change this from login to signup!
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>
}