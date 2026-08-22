package com.minimize.uniswap.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor() {
    
    private val TAG = "GoogleAuthHelper"

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
            Log.d(TAG, "Launching Google Sign-In with Web Client ID: $webClientId")
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            when {
                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(TAG, "Google ID Token retrieved successfully.")
                        Result.success(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Failed to parse Google ID Token: ${e.message}")
                        Result.failure(Exception("Failed to parse Google ID Token", e))
                    }
                }
                credential is GoogleIdTokenCredential -> {
                    Log.d(TAG, "Google ID Token retrieved successfully.")
                    Result.success(credential.idToken)
                }
                else -> {
                    val errorMsg = "Unexpected credential type: ${credential.type}"
                    Log.e(TAG, errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In was cancelled by the user.")
            Result.failure(Exception("Sign-in was cancelled."))
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            val errorMsg = "No Google account found on this device or SHA-1 fingerprint is not configured in Firebase Console."
            Log.e(TAG, "$errorMsg (${e.message})")
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val isDevError = e.message?.contains("10", ignoreCase = true) == true || e.message?.contains("Developer error", ignoreCase = true) == true
            val errorMsg = if (isDevError) {
                "Google Sign-In Developer Error (10): Ensure your debug SHA-1 fingerprint is added in Firebase Console."
            } else {
                e.localizedMessage ?: "Google Sign-In failed."
            }
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            Result.failure(Exception(errorMsg, e))
        }
    }
}
