package com.minimize.uniswap.data.repository


import com.minimize.uniswap.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String, displayName: String = ""): Result<String>
    suspend fun authenticate(email: String, password: String): Result<String>
    suspend fun signInWithGoogle(idToken: String): Result<String>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentUser(): User?
    fun getUserFlow(): Flow<User?>
    suspend fun sendVerificationEmail(): Result<Unit>
    suspend fun reloadUser(): Result<Unit>
    suspend fun updateAvatar(avatarId: String): Result<Unit>
    suspend fun updateCampusCenter(campusCenter: String): Result<Unit>
}
