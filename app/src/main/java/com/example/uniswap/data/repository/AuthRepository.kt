package com.example.uniswap.data.repository

import com.example.uniswap.data.api.RetrofitClient
import com.example.uniswap.data.local.TokenManager
import com.example.uniswap.data.model.AuthResponse
import com.example.uniswap.data.model.LoginRequest
import com.example.uniswap.data.model.SignupRequest
import retrofit2.Response

class AuthRepository(private val tokenManager: TokenManager) {

    // 1. SIGNUP
    suspend fun signup(request: SignupRequest): Result<String> {
        return try {
            val response = RetrofitClient.authApi.signup(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Result.failure(Exception("Cannot connect to server. Is Spring Boot running?"))
        }
    }

    // 2. LOGIN
    suspend fun login(request: LoginRequest): Result<String> {
        return try {
            val response = RetrofitClient.authApi.login(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Result.failure(Exception("Network error. Check if Spring Boot is running at 10.0.2.2"))
        }
    }

    /**
     * Helper function to process the AuthResponse, save the JWT, and return the result.
     */
    private suspend fun handleAuthResponse(response: Response<AuthResponse>): Result<String> {
        return  if (response.isSuccessful) {
            val body = response.body()
            println("LOGCAT_AUTH: Response Successful. Message: ${body?.message}")
            println("LOGCAT_AUTH: Token Received: ${body?.token?.take(10)}...") // Shows first 10 chars

            body?.token?.let {
                println("LOGCAT_AUTH: Attempting to save token to DataStore...")
                tokenManager.saveToken(it)
            }
            Result.success(body?.message ?: "Success")
        } else {
            val error = response.errorBody()?.string()
            println("LOGCAT_AUTH: Response Failed. Error: $error")
            Result.failure(Exception(error))
        }
    }
}