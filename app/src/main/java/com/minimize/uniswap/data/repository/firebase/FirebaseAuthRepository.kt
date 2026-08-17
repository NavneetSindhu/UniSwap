package com.minimize.uniswap.data.repository.firebase

import com.minimize.uniswap.data.model.LoginRequest
import com.minimize.uniswap.data.model.SignupRequest
import com.minimize.uniswap.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signup(request: SignupRequest): Result<String> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(request.email, request.password).await()
            Result.success("User created successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<String> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(request.email, request.password).await()
            Result.success("Login successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
