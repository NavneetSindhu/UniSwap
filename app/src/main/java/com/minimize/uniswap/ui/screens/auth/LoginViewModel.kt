package com.minimize.uniswap.ui.screens.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.LoginRequest
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

            val request = LoginRequest(email, password)

            // The repository now handles authentication via FirebaseAuth
            val result = repository.login(request)

            result.onSuccess {
                isSuccess = true
            }.onFailure {
                errorMessage = it.message
            }
            isLoading = false
        }
    }

    fun onGoogleLoginClick(webClientId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val idToken = googleAuthHelper.getGoogleIdToken(webClientId)
            if (idToken != null) {
                val result = repository.signInWithGoogle(idToken)
                result.onSuccess {
                    isSuccess = true
                }.onFailure {
                    errorMessage = it.message ?: "Google Login failed"
                }
            } else {
                errorMessage = "Could not get Google ID Token"
            }
            isLoading = false
        }
    }
}
