package com.minimize.uniswap.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor() {

    suspend fun getGoogleIdToken(context: Context, webClientId: String): Result<String> {
        val credentialManager = CredentialManager.create(context)
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            Timber.d("Launching Google Sign-In with Web Client ID: %s", webClientId)
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            when {
                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Timber.d("Google ID Token retrieved successfully.")
                        Result.success(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Timber.e(e, "Failed to parse Google ID Token: %s", e.message)
                        Result.failure(Exception("Failed to parse Google ID Token", e))
                    }
                }
                credential is GoogleIdTokenCredential -> {
                    Timber.d("Google ID Token retrieved successfully.")
                    Result.success(credential.idToken)
                }
                else -> {
                    val errorMsg = "Unexpected credential type: ${credential.type}"
                    Timber.e(errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Timber.d("Google Sign-In was cancelled by the user.")
            Result.failure(Exception("Sign-in was cancelled."))
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            val errorMsg = "No Google account found on this device or SHA-1 fingerprint is not configured in Firebase Console."
            Timber.e(e, "%s (%s)", errorMsg, e.message)
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val isDevError = e.message?.contains("10", ignoreCase = true) == true || e.message?.contains("Developer error", ignoreCase = true) == true
            val errorMsg = if (isDevError) {
                "Google Sign-In Developer Error (10): Ensure your debug SHA-1 fingerprint is added in Firebase Console."
            } else {
                e.localizedMessage ?: "Google Sign-In failed."
            }
            Timber.e(e, "Credential Manager error: %s", e.message)
            Result.failure(Exception(errorMsg, e))
        }
    }
}
