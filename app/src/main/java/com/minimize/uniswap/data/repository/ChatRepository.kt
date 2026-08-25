package com.minimize.uniswap.data.repository

import com.minimize.uniswap.data.model.ChatThread
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
     * Observes real-time chat threads/conversations for a specific user.
     */
    fun getChatThreadsFlow(userId: String): Flow<List<ChatThread>>

    /**
     * Sends a new message to the chat thread and updates conversation metadata.
     */
    suspend fun sendMessage(
        itemId: String,
        buyerId: String,
        sellerId: String,
        message: Message,
        itemTitle: String = "",
        itemImageUrl: String = "",
        buyerName: String = "",
        sellerName: String = ""
    ): Boolean

    /**
     * Edits the text of an existing message.
     */
    suspend fun editMessage(
        itemId: String,
        buyerId: String,
        sellerId: String,
        messageId: String,
        newText: String
    ): Result<Unit>

    /**
     * Soft deletes a message (marks as deleted).
     */
    suspend fun deleteMessage(
        itemId: String,
        buyerId: String,
        sellerId: String,
        messageId: String
    ): Result<Unit>

    /**
     * Deletes / hides a conversation thread for the current user.
     */
    suspend fun deleteConversation(
        chatId: String,
        userId: String
    ): Result<Unit>

    /**
     * Clears all local chat history for the specified conversation.
     */
    suspend fun clearChatHistory(
        itemId: String,
        buyerId: String,
        sellerId: String,
        userId: String
    ): Result<Unit>

    /**
     * Marks all unread messages in the conversation as read by the current user.
     */
    suspend fun markChatAsRead(
        itemId: String,
        buyerId: String,
        sellerId: String,
        currentUserId: String
    ): Result<Unit>
}

