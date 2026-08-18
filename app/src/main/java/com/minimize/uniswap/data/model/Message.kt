package com.minimize.uniswap.data.model

import com.google.firebase.Timestamp
import java.util.UUID

enum class MessageStatus {
    SENDING, SENT, FAILED
}

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val firestoreTimestamp: Timestamp? = null, // Used for server-side sorting
    val isLocationPin: Boolean = false,
    val locationName: String? = null,
    val status: MessageStatus = MessageStatus.SENT
)
