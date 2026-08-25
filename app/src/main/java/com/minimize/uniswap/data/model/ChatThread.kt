package com.minimize.uniswap.data.model

data class ChatThread(
    val id: String = "",
    val itemId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val buyerName: String = "Buyer",
    val sellerName: String = "Seller",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastSenderId: String = "",
    val itemTitle: String = "",
    val itemImageUrl: String = "",
    val deletedForUserIds: List<String> = emptyList(),
    val unreadByParticipantIds: List<String> = emptyList(),
    val lastMessageStatus: MessageStatus = MessageStatus.SENT
)
