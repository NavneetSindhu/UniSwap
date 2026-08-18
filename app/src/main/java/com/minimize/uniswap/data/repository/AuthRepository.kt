package com.minimize.uniswap.data.repository


interface AuthRepository {
    suspend fun authenticate(email: String, password: String): Result<String>
    suspend fun signInWithGoogle(idToken: String): Result<String>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}
