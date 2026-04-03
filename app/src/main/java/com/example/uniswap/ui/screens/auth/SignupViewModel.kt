package com.example.uniswap.ui.screens.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.SignupRequest
import com.example.uniswap.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var department by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun onSignupClick() {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val request = SignupRequest(fullName, email, password, department)
            val result = repository.signup(request)

            result.onSuccess {
                isSuccess = true
            }.onFailure {
                errorMessage = it.message ?: "Signup failed. Please try again."
            }
            isLoading = false
        }
    }
}