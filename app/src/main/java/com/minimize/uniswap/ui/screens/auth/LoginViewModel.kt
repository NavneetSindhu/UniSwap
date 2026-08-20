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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val googleAuthHelper: GoogleAuthHelper
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun onLoginClick() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.authenticate(email.trim(), password.trim())

            result.onSuccess {
                isSuccess = true
            }.onFailure {
                errorMessage = it.message ?: "Authentication failed"
            }
            isLoading = false
        }
    }

    fun onGoogleLoginClick(context: Context) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val idToken = googleAuthHelper.getGoogleIdToken(context, BuildConfig.WEB_CLIENT_ID)
            if (idToken != null) {
                val result = repository.signInWithGoogle(idToken)
                result.onSuccess {
                    isSuccess = true
                }.onFailure {
                    errorMessage = it.message ?: "Google Login failed"
                }
            } else {
                errorMessage = "Google ID Token not found. Check Logcat for details."
            }
            isLoading = false
        }
    }
}
