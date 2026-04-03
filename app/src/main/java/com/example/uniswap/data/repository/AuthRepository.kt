package com.example.uniswap.data.repository

import com.example.uniswap.data.api.RetrofitClient
import com.example.uniswap.data.model.LoginRequest
import com.example.uniswap.data.model.SignupRequest
import java.lang.Exception

class AuthRepository {

    // 1. SIGNUP LOGIC (POST)
    suspend fun signup(request: SignupRequest): Result<String> {
        return try {
            val response = RetrofitClient.authApi.signup(request)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "User registered successfully!")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Signup failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Cannot connect to server. Is Spring Boot running?"))
        }
    }

    // 2. LOGIN LOGIC (Now updated to POST)
    suspend fun login(request: LoginRequest): Result<String> {
        return try {
            // We pass the whole 'request' object now, not individual strings
            val response = RetrofitClient.authApi.login(request)

            if (response.isSuccessful) {
                // Captures "Login Successful! Welcome..." from Spring Boot
                Result.success(response.body() ?: "Login successful")
            } else {
                // Captures 401 "Invalid Email or Password" from your controller
                val errorMsg = response.errorBody()?.string() ?: "Invalid credentials"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error. Check if Spring Boot is running at 10.0.2.2"))
        }
    }
}