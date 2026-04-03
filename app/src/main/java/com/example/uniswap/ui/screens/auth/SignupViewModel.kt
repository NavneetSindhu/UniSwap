package com.example.uniswap.ui.screens.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.SignupRequest
import com.example.uniswap.data.repository.AuthRepository
import kotlinx.coroutines.launch

// 1. Remove the "= AuthRepository()" default value.
// Now it MUST be provided by the Factory.
class SignupViewModel(private val repository: AuthRepository) : ViewModel() {

    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var department by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun onSignupClick() {
        // Validation check
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || department.isBlank()) {
            errorMessage = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // The repository.signup() now automatically saves the JWT
            // to DataStore thanks to the TokenManager we added earlier!
            val request = SignupRequest(fullName, email, password, department)
            val result = repository.signup(request)

            result.onSuccess {
                isSuccess = true
            }.onFailure {
                // Extracts the "Email already in use" or other errors from Spring
                errorMessage = it.message ?: "Signup failed. Please try again."
            }
            isLoading = false
        }
    }
}