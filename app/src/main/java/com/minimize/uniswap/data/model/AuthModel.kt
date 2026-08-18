package com.minimize.uniswap.data.model

/**
 * Request model for User Authentication.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
