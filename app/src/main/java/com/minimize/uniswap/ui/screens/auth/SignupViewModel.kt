package com.minimize.uniswap.ui.screens.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.SignupRequest
import com.minimize.uniswap.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

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

            // The repository.signup() handles authentication via FirebaseAuth
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
