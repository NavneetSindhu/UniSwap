package com.minimize.uniswap.data.model

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isLocationPin: Boolean = false,
    val locationName: String? = null
)
