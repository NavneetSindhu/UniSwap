package com.minimize.uniswap.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor() {
    
    private val TAG = "GoogleAuthHelper"

    suspend fun getGoogleIdToken(context: Context, webClientId: String): String? {
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
            if (credential is GoogleIdTokenCredential) {
                Log.d(TAG, "Google ID Token retrieved successfully.")
                credential.idToken
            } else {
                Log.e(TAG, "Retrieved credential is not GoogleIdTokenCredential: ${credential.type}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Credential Manager error: ${e.message}")
            null
        }
    }
}
