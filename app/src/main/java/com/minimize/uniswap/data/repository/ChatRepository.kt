package com.minimize.uniswap.data.repository

import com.minimize.uniswap.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing real-time chat messages between buyers and sellers.
 */
interface ChatRepository {
    
    /**
     * Observes real-time messages for a specific item between two users.
     */
    fun getMessages(itemId: String, buyerId: String, sellerId: String): Flow<List<Message>>

    /**
     * Sends a new message to the chat thread.
     */
    suspend fun sendMessage(itemId: String, buyerId: String, sellerId: String, message: Message): Boolean
}
