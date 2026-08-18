package com.minimize.uniswap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minimize.uniswap.data.model.MessageStatus

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: MessageStatus,
    val isLocationPin: Boolean = false,
    val locationName: String? = null
)
