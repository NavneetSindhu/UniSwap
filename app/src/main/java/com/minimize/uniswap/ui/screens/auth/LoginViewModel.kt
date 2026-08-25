package com.minimize.uniswap.ui.screens.auth

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.BuildConfig
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.util.GoogleAuthHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val googleAuthHelper: GoogleAuthHelper
) : ViewModel() {

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var college by mutableStateOf("Main Campus Center")
    var branch by mutableStateOf("Computer Science & Eng")
    var batch by mutableStateOf("2026")
    var isSignUpMode by mutableStateOf(false)

    var isEmailLoading by mutableStateOf(false)
    var isGoogleLoading by mutableStateOf(false)
    val isLoading get() = isEmailLoading || isGoogleLoading
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun toggleAuthMode() {
        isSignUpMode = !isSignUpMode
        errorMessage = null
    }

    fun onSignInClick() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Please enter a valid email address"
            return
        }

        if (trimmedPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        viewModelScope.launch {
            isEmailLoading = true
            errorMessage = null
            Timber.d("onSignInClick: attempting login for %s", trimmedEmail)

            val result = repository.login(trimmedEmail, trimmedPassword)
            result.onSuccess {
                Timber.i("Login successful for %s", trimmedEmail)
                isSuccess = true
            }.onFailure {
                Timber.e(it, "Login failed for %s: %s", trimmedEmail, it.message)
                errorMessage = it.message ?: "Sign In failed"
            }
            isEmailLoading = false
        }
    }

    fun onSignUpClick() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            errorMessage = "Please enter your full name"
            return
        }

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Please enter a valid university email address"
            return
        }

        if (trimmedPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        viewModelScope.launch {
            isEmailLoading = true
            errorMessage = null
            Timber.d("onSignUpClick: attempting signup for %s (%s)", trimmedName, trimmedEmail)

            val result = repository.signUp(trimmedEmail, trimmedPassword, trimmedName)
            result.onSuccess {
                Timber.i("Signup successful for %s", trimmedEmail)
                isSuccess = true
            }.onFailure {
                Timber.e(it, "Signup failed for %s: %s", trimmedEmail, it.message)
                errorMessage = it.message ?: "Sign Up failed"
            }
            isEmailLoading = false
        }
    }

    fun onSubmitClick() {
        if (isSignUpMode) onSignUpClick() else onSignInClick()
    }

    // Keep onLoginClick alias for backwards compatibility
    fun onLoginClick() = onSubmitClick()

    fun onGoogleLoginClick(context: Context) {
        viewModelScope.launch {
            isGoogleLoading = true
            errorMessage = null
            Timber.d("Launching Google Sign-In with Web Client ID: %s", BuildConfig.WEB_CLIENT_ID)

            val tokenResult = googleAuthHelper.getGoogleIdToken(context, BuildConfig.WEB_CLIENT_ID)
            tokenResult.onSuccess { idToken ->
                Timber.d("Google ID token retrieved, signing into Firebase...")
                val authResult = repository.signInWithGoogle(idToken)
                authResult.onSuccess {
                    Timber.i("Google Sign-In successful")
                    isSuccess = true
                }.onFailure {
                    Timber.e(it, "Firebase Google Auth failed: %s", it.message)
                    errorMessage = it.message ?: "Google Authentication failed"
                }
            }.onFailure {
                Timber.e(it, "Google ID token retrieval failed: %s", it.message)
                errorMessage = it.message ?: "Google Sign-In failed"
            }
            isGoogleLoading = false
        }
    }
}
