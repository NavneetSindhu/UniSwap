package com.minimize.uniswap.data.repository.firebase

import android.util.Log
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val TAG = "FirebaseAuthRepo"

    override suspend fun authenticate(email: String, password: String): Result<String> {
        return try {
            Log.d(TAG, "Attempting login for: $email")
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            syncUserToFirestore()
            Result.success("Login successful")
        } catch (e: Exception) {
            Log.w(TAG, "Login failed: ${e.message}. Checking if signup is possible.")
            // Catch "User not found" or general invalid user exceptions
            if (e is FirebaseAuthInvalidUserException || e.message?.contains("no user", ignoreCase = true) == true) {
                try {
                    Log.d(TAG, "User not found. Attempting signup for: $email")
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    syncUserToFirestore()
                    Result.success("Account created successfully")
                } catch (signupError: Exception) {
                    Log.e(TAG, "Signup failed: ${signupError.message}")
                    Result.failure(signupError)
                }
            } else {
                Log.e(TAG, "Authentication failed with unexpected error: ${e.message}")
                Result.failure(e)
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            Log.d(TAG, "Attempting Google sign-in with token: ${idToken.take(10)}...")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            syncUserToFirestore()
            Result.success("Google sign-in successful")
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed: ${e.message}")
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

    override fun getUserFlow(): Flow<User?> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(User::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            syncUserToFirestore() 
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncUserToFirestore() {
        val firebaseUser = firebaseAuth.currentUser ?: return
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        
        Log.d(TAG, "Syncing user to Firestore: ${firebaseUser.uid}")
        
        val snapshot = userRef.get().await()
        val user = if (snapshot.exists()) {
            snapshot.toObject(User::class.java)?.copy(
                // FOR DEBUGGING: Force emailVerified = true
                isEmailVerified = true // firebaseUser.isEmailVerified
            )
        } else {
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "Campus User",
                // FOR DEBUGGING: Force emailVerified = true
                isEmailVerified = true // firebaseUser.isEmailVerified
            )
        }
        
        user?.let { 
            userRef.set(it).await()
            Log.d(TAG, "User synced successfully.")
        }
    }
}
