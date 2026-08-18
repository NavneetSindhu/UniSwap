package com.minimize.uniswap.data.repository.firebase

import com.minimize.uniswap.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun authenticate(email: String, password: String): Result<String> {
        return try {
            // 1. Try to sign in
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success("Login successful")
        } catch (e: FirebaseAuthInvalidUserException) {
            // 2. User doesn't exist, try to sign up
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                Result.success("Account created successfully")
            } catch (signupError: Exception) {
                Result.failure(signupError)
            }
        } catch (e: Exception) {
            // 3. Other errors (e.g. wrong password)
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success("Google sign-in successful")
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
