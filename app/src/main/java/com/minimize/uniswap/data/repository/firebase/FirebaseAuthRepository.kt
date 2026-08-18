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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun authenticate(email: String, password: String): Result<String> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            syncUserToFirestore()
            Result.success("Login successful")
        } catch (e: FirebaseAuthInvalidUserException) {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                syncUserToFirestore()
                Result.success("Account created successfully")
            } catch (signupError: Exception) {
                Result.failure(signupError)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            syncUserToFirestore()
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
            syncUserToFirestore() // Sync the new emailVerified status to Firestore
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncUserToFirestore() {
        val firebaseUser = firebaseAuth.currentUser ?: return
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        
        val snapshot = userRef.get().await()
        val user = if (snapshot.exists()) {
            snapshot.toObject(User::class.java)?.copy(
                isEmailVerified = firebaseUser.isEmailVerified
            )
        } else {
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "Campus User",
                isEmailVerified = firebaseUser.isEmailVerified
            )
        }
        
        user?.let { userRef.set(it).await() }
    }
}
