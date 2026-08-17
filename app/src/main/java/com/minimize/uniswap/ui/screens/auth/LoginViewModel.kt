package com.minimize.uniswap.ui.screens.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.LoginRequest
import com.minimize.uniswap.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

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

            // 2. The repository now handles the JWT storage automatically!
            val result = repository.login(request)

            result.onSuccess {
                println("LOGCAT_VIEWMODEL: Login Success! Setting isSuccess to true.")
                isSuccess = true
            }.onFailure {
                println("LOGCAT_VIEWMODEL: Login Failed! Error: ${it.message}")
                errorMessage = it.message
            }
            isLoading = false
        }
    }
}
