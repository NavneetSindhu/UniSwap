package com.minimize.uniswap.data.repository.firebase

import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferencesManager: UserPreferencesManager
) : AuthRepository {

    private val _isGuestMode = MutableStateFlow(false)
    override val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager.isGuestModeFlow.collect { isGuest ->
                _isGuestMode.value = isGuest
            }
        }
    }

    override suspend fun continueAsGuest() {
        preferencesManager.updateGuestMode(true)
        _isGuestMode.value = true
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null || _isGuestMode.value
    }

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            Timber.d("Attempting login for: %s", email)
            firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            preferencesManager.updateGuestMode(false)
            _isGuestMode.value = false
            syncUserToFirestore()
            Result.success("Login successful")
        } catch (e: Exception) {
            Timber.e(e, "Login failed: %s", e.message)
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        avatarId: String?,
        campusCenter: String?
    ): Result<String> {
        return try {
            Timber.d("Attempting signup for: %s", email)
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            preferencesManager.updateGuestMode(false)
            _isGuestMode.value = false
            if (displayName.isNotBlank()) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                authResult.user?.updateProfile(profileUpdates)?.await()
            }
            syncUserToFirestore(
                customDisplayName = displayName.ifBlank { null },
                customAvatarId = avatarId,
                customCampusCenter = campusCenter
            )
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
            preferencesManager.updateGuestMode(false)
            _isGuestMode.value = false
            syncUserToFirestore()
            Result.success("Google sign-in successful")
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in failed: %s", e.message)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        preferencesManager.updateGuestMode(false)
        _isGuestMode.value = false
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

    private fun isTestVerificationEmail(email: String): Boolean {
        val trimmed = email.trim().lowercase()
        return trimmed == "test@campus.edu" ||
                trimmed == "demo@student.edu" ||
                trimmed.endsWith("@uniswap.test") ||
                trimmed.startsWith("test@") ||
                trimmed.startsWith("test.")
    }

    override suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            val email = user?.email.orEmpty()
            if (isTestVerificationEmail(email) || user == null) {
                Timber.d("sendVerificationEmail: Test email bypass for %s", email)
                return Result.success(Unit)
            }
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            val email = user?.email.orEmpty()
            if (isTestVerificationEmail(email)) {
                Timber.d("reloadUser: Test email bypass - auto-verifying %s", email)
                preferencesManager.updateVerificationStatus(true)
                if (user != null) {
                    firestore.collection("users").document(user.uid)
                        .update("isEmailVerified", true)
                        .await()
                }
                return Result.success(Unit)
            }
            user?.reload()?.await()
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

    override suspend fun updateCampusCenter(campusCenter: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            firestore.collection("users").document(uid)
                .update("campusCenter", campusCenter)
                .await()
            Timber.d("User campus center updated to %s", campusCenter)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update campus center: %s", e.message)
            Result.failure(e)
        }
    }

    private suspend fun syncUserToFirestore(
        customDisplayName: String? = null,
        customAvatarId: String? = null,
        customCampusCenter: String? = null
    ) {
        val firebaseUser = firebaseAuth.currentUser ?: return
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        
        Timber.d("Syncing user to Firestore: %s", firebaseUser.uid)
        
        try {
            val snapshot = userRef.get().await()
            val user = if (snapshot.exists()) {
                val existing = snapshot.toObject(User::class.java)
                existing?.copy(
                    displayName = customDisplayName ?: existing.displayName.ifBlank { firebaseUser.displayName ?: "Campus User" },
                    avatarId = customAvatarId ?: existing.avatarId,
                    campusCenter = customCampusCenter ?: existing.campusCenter,
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            } else {
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = customDisplayName ?: firebaseUser.displayName ?: "Campus User",
                    avatarId = customAvatarId ?: "avatar_scholar",
                    campusCenter = customCampusCenter ?: "Main Campus Center",
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            }
            
            user?.let { 
                userRef.set(it).await()
                Timber.d("User synced successfully.")
                
                // Sync FCM Token for push notifications
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        if (!token.isNullOrBlank()) {
                            userRef.update("fcmToken", token)
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Could not fetch FCM token on user sync")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync user to Firestore: %s", e.message)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("No user currently signed in"))
        val uid = user.uid
        return try {
            Timber.i("Starting account deletion for user: %s", uid)
            // 1. Delete user's active listings
            val itemsSnapshot = firestore.collection("items")
                .whereEqualTo("sellerId", uid)
                .get()
                .await()
            for (doc in itemsSnapshot.documents) {
                doc.reference.delete().await()
            }
            // 2. Delete user's profile document from Firestore
            firestore.collection("users").document(uid).delete().await()
            // 3. Delete Firebase Auth account
            user.delete().await()
            // 4. Clear local preferences and state
            preferencesManager.clearAll()
            _isGuestMode.value = false
            Timber.i("Account deletion completed successfully for user: %s", uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete account: %s", e.message)
            val mapped = if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                Exception("For security reasons, please log in again before deleting your account.", e)
            } else {
                mapAuthException(e)
            }
            Result.failure(mapped)
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
