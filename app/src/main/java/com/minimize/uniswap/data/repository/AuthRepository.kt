package com.minimize.uniswap.data.repository

import com.minimize.uniswap.data.model.LoginRequest
import com.minimize.uniswap.data.model.SignupRequest

interface AuthRepository {
    suspend fun signup(request: SignupRequest): Result<String>
    suspend fun login(request: LoginRequest): Result<String>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
}
