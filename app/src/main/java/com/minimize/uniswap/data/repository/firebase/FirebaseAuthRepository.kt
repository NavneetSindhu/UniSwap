package com.minimize.uniswap.data.repository.firebase

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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            Timber.d("Attempting login for: %s", email)
            firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            syncUserToFirestore()
            Result.success("Login successful")
        } catch (e: Exception) {
            Timber.e(e, "Login failed: %s", e.message)
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<String> {
        return try {
            Timber.d("Attempting signup for: %s", email)
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            if (displayName.isNotBlank()) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                authResult.user?.updateProfile(profileUpdates)?.await()
            }
            syncUserToFirestore(customDisplayName = displayName.ifBlank { null })
            Result.success("Account created successfully")
        } catch (e: Exception) {
            Timber.e(e, "Signup failed: %s", e.message)
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun authenticate(email: String, password: String): Result<String> {
        return login(email, password)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            Timber.d("Attempting Google sign-in with token: %s...", idToken.take(10))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            syncUserToFirestore()
            Result.success("Google sign-in successful")
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in failed: %s", e.message)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getCurrentUser(): User? {
        val fbUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = fbUser.uid,
            email = fbUser.email ?: "",
            displayName = fbUser.displayName ?: "",
            isEmailVerified = fbUser.isEmailVerified,
            profilePicUrl = fbUser.photoUrl?.toString() ?: ""
        )
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
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            syncUserToFirestore() 
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun updateAvatar(avatarId: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            firestore.collection("users").document(uid)
                .update("avatarId", avatarId)
                .await()
            Timber.d("User avatar updated to %s", avatarId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update avatar: %s", e.message)
            Result.failure(e)
        }
    }

    private suspend fun syncUserToFirestore(customDisplayName: String? = null) {
        val firebaseUser = firebaseAuth.currentUser ?: return
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        
        Timber.d("Syncing user to Firestore: %s", firebaseUser.uid)
        
        try {
            val snapshot = userRef.get().await()
            val user = if (snapshot.exists()) {
                val existing = snapshot.toObject(User::class.java)
                existing?.copy(
                    displayName = customDisplayName ?: existing.displayName.ifBlank { firebaseUser.displayName ?: "Campus User" },
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            } else {
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = customDisplayName ?: firebaseUser.displayName ?: "Campus User",
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            }
            
            user?.let { 
                userRef.set(it).await()
                Timber.d("User synced successfully.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync user to Firestore: %s", e.message)
        }
    }

    private fun mapAuthException(e: Exception): Exception {
        val message = when {
            e is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
                "Password should be at least 6 characters."
            e is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                "An account with this email already exists. Please sign in."
            e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                "Invalid email or password. Please verify and try again."
            e is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                "No account found with this email address."
            e is com.google.firebase.FirebaseNetworkException ->
                "Network error. Please check your internet connection."
            e.message?.contains("badly formatted", ignoreCase = true) == true ->
                "Please enter a valid email address."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
        return Exception(message, e)
    }
}
