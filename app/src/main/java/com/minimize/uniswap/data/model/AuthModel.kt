package com.minimize.uniswap.data.model

/**
 * Request model for User Signup.
 * Used for local validation and parameter passing.
 */
data class SignupRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val department: String
)

/**
 * Request model for User Login.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
