package com.example.uniswap.data.model

import com.google.gson.annotations.SerializedName

// This matches your Spring Boot SignupRequest DTO exactly
data class SignupRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val department: String
)

// To handle success/error messages from the server

data class AuthResponse(
    val token: String,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String,

)