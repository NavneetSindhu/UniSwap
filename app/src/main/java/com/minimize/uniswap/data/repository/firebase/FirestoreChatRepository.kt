package com.minimize.uniswap.data.repository.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.minimize.uniswap.data.local.dao.MessageDao
import com.minimize.uniswap.data.local.entity.MessageEntity
import com.minimize.uniswap.data.model.ChatThread
import com.minimize.uniswap.data.model.Message
import com.minimize.uniswap.data.model.MessageStatus
import com.minimize.uniswap.data.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messageDao: MessageDao,
    private val applicationScope: CoroutineScope
) : ChatRepository {

    /**
     * Generates a deterministic ID for 1-to-1 chats for a specific item.
     * Logic: itemId_min(uid1, uid2)_max(uid1, uid2)
     */
    private fun getChatId(itemId: String, userId1: String, userId2: String): String {
        val uids = listOf(userId1, userId2).sorted()
        return "__"
    }

    override fun getMessages(itemId: String, buyerId: String, sellerId: String): Flow<List<Message>> {
        val chatId = getChatId(itemId, buyerId, sellerId)
        
        // Start Firestore listener to sync to Room in the background
        startSyncListener(chatId)

        // Return local Room data as the source of truth
        return messageDao.getMessagesForChat(chatId)
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
    }

    override fun getChatThreadsFlow(userId: String): Flow<List<ChatThread>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val subscription = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val threads = snapshot.documents.mapNotNull { doc ->
                        doc.toChatThread()
                    }.filter { thread ->
                        !thread.deletedForUserIds.contains(userId)
                    }.sortedByDescending { it.lastMessageTimestamp }
                    trySend(threads)
                }
            }
        awaitClose { subscription.remove() }
    }

    private fun startSyncListener(chatId: String) {
        val chatRef = firestore.collection("chats").document(chatId).collection("messages")
        
        applicationScope.launch {
            callbackFlow {
                val subscription = chatRef
                    .orderBy("firestoreTimestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val messages = snapshot.documents.mapNotNull { doc ->
                                doc.toChatMessage()
                            }
                            trySend(messages)
                        }
                    }
                awaitClose { subscription.remove() }
            }.collect { remoteMessages ->
                val entities = remoteMessages.map { it.toEntity(chatId) }
                messageDao.insertMessages(entities)
            }
        }
    }

    override suspend fun sendMessage(
        itemId: String,
        buyerId: String,
        sellerId: String,
        message: Message,
        itemTitle: String,
        itemImageUrl: String,
        buyerName: String,
        sellerName: String
    ): Boolean {
        val chatId = getChatId(itemId, buyerId, sellerId)
        val messageId = message.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val messageToSend = message.copy(id = messageId)
        val entity = messageToSend.toEntity(chatId).copy(status = MessageStatus.SENDING)
        
        // 1. Optimistic update to Room
        messageDao.insertMessage(entity)

        return try {
            val receiverId = if (messageToSend.senderId == buyerId) sellerId else buyerId

            // 2. Update/Create parent chat thread metadata document
            val chatMetadata = hashMapOf<String, Any>(
                "id" to chatId,
                "itemId" to itemId,
                "buyerId" to buyerId,
                "sellerId" to sellerId,
                "participants" to listOf(buyerId, sellerId),
                "lastMessage" to messageToSend.text,
                "lastMessageTimestamp" to messageToSend.timestamp,
                "lastSenderId" to messageToSend.senderId,
                "lastMessageStatus" to MessageStatus.SENT.name,
                "unreadByParticipantIds" to listOf(receiverId),
                "deletedForUserIds" to emptyList<String>()
            )
            if (itemTitle.isNotBlank()) chatMetadata["itemTitle"] = itemTitle
            if (itemImageUrl.isNotBlank()) chatMetadata["itemImageUrl"] = itemImageUrl
            if (buyerName.isNotBlank()) chatMetadata["buyerName"] = buyerName
            if (sellerName.isNotBlank()) chatMetadata["sellerName"] = sellerName

            firestore.collection("chats")
                .document(chatId)
                .set(chatMetadata, SetOptions.merge())
                .await()

            // 3. Prepare Firestore message data with server timestamp and matching ID
            val messageData = hashMapOf(
                "id" to messageId,
                "senderId" to messageToSend.senderId,
                "text" to messageToSend.text,
                "timestamp" to messageToSend.timestamp,
                "firestoreTimestamp" to FieldValue.serverTimestamp(),
                "isLocationPin" to messageToSend.isLocationPin,
                "locationName" to messageToSend.locationName,
                "isEdited" to false,
                "isDeleted" to false,
                "status" to MessageStatus.SENT.name,
                "readAt" to null,
                "deliveredAt" to null
            )

            // 4. Send to Firestore subcollection
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(messageData)
                .await()

            // 5. Update Room status to SENT
            messageDao.updateMessageStatus(messageId, MessageStatus.SENT)
            
            true
        } catch (e: Exception) {
            // 6. Update Room status to FAILED
            messageDao.updateMessageStatus(messageId, MessageStatus.FAILED)
            false
        }
    }

    override suspend fun editMessage(
        itemId: String,
        buyerId: String,
        sellerId: String,
        messageId: String,
        newText: String
    ): Result<Unit> = runCatching {
        val chatId = getChatId(itemId, buyerId, sellerId)
        
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                mapOf(
                    "text" to newText.trim(),
                    "isEdited" to true
                )
            )
            .await()

        // Also update thread lastMessage if this was the last message
        firestore.collection("chats")
            .document(chatId)
            .update("lastMessage", newText.trim())
            .await()
    }

    override suspend fun deleteMessage(
        itemId: String,
        buyerId: String,
        sellerId: String,
        messageId: String
    ): Result<Unit> = runCatching {
        val chatId = getChatId(itemId, buyerId, sellerId)
        val placeholder = "This message was deleted"

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                mapOf(
                    "text" to placeholder,
                    "isDeleted" to true
                )
            )
            .await()

        firestore.collection("chats")
            .document(chatId)
            .update("lastMessage", placeholder)
            .await()
    }

    override suspend fun deleteConversation(
        chatId: String,
        userId: String
    ): Result<Unit> = runCatching {
        if (chatId.isBlank() || userId.isBlank()) return@runCatching

        // Add user to deletedForUserIds array so it is hidden from their inbox
        firestore.collection("chats")
            .document(chatId)
            .update("deletedForUserIds", FieldValue.arrayUnion(userId))
            .await()

        // Clean up local messages for this chat
        messageDao.deleteMessagesForChat(chatId)
    }

    override suspend fun clearChatHistory(
        itemId: String,
        buyerId: String,
        sellerId: String,
        userId: String
    ): Result<Unit> = runCatching {
        val chatId = getChatId(itemId, buyerId, sellerId)
        messageDao.deleteMessagesForChat(chatId)
    }

    override suspend fun markChatAsRead(
        itemId: String,
        buyerId: String,
        sellerId: String,
        currentUserId: String
    ): Result<Unit> = runCatching {
        if (currentUserId.isBlank()) return@runCatching
        val chatId = getChatId(itemId, buyerId, sellerId)

        // 1. Remove current user from thread's unread list
        firestore.collection("chats")
            .document(chatId)
            .update(
                mapOf(
                    "unreadByParticipantIds" to FieldValue.arrayRemove(currentUserId),
                    "lastMessageStatus" to MessageStatus.READ.name
                )
            )
            .await()

        // 2. Mark incoming unread messages as read
        val unreadSnapshot = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereNotEqualTo("senderId", currentUserId)
            .get()
            .await()

        if (!unreadSnapshot.isEmpty) {
            val batch = firestore.batch()
            val now = System.currentTimeMillis()
            var hasUpdates = false

            unreadSnapshot.documents.forEach { doc ->
                if (doc.getLong("readAt") == null) {
                    batch.update(
                        doc.reference,
                        mapOf(
                            "readAt" to now,
                            "status" to MessageStatus.READ.name
                        )
                    )
                    hasUpdates = true
                }
            }

            if (hasUpdates) {
                batch.commit().await()
            }
        }
    }

    // Extension functions for mapping
    private fun MessageEntity.toDomainModel() = Message(
        id = id,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = status,
        isLocationPin = isLocationPin,
        locationName = locationName,
        isEdited = isEdited,
        isDeleted = isDeleted,
        readAt = readAt,
        deliveredAt = deliveredAt
    )

    private fun Message.toEntity(chatId: String) = MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = status,
        isLocationPin = isLocationPin,
        locationName = locationName,
        isEdited = isEdited,
        isDeleted = isDeleted,
        readAt = readAt,
        deliveredAt = deliveredAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toChatThread(): ChatThread? {
        if (!exists()) return null
        return try {
            val rawTimestamp = get("lastMessageTimestamp")
            val timestampMillis = when (rawTimestamp) {
                is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                is Number -> rawTimestamp.toLong()
                else -> 0L
            }

            val participantsList = (get("participants") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val deletedList = (get("deletedForUserIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val unreadList = (get("unreadByParticipantIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val rawStatus = getString("lastMessageStatus")
            val lastStatus = try {
                if (rawStatus != null) MessageStatus.valueOf(rawStatus) else MessageStatus.SENT
            } catch (e: Exception) {
                MessageStatus.SENT
            }

            ChatThread(
                id = getString("id") ?: id,
                itemId = getString("itemId") ?: "",
                buyerId = getString("buyerId") ?: "",
                sellerId = getString("sellerId") ?: "",
                buyerName = getString("buyerName") ?: "Buyer",
                sellerName = getString("sellerName") ?: "Seller",
                participants = participantsList,
                lastMessage = getString("lastMessage") ?: "",
                lastMessageTimestamp = timestampMillis,
                lastSenderId = getString("lastSenderId") ?: "",
                itemTitle = getString("itemTitle") ?: "",
                itemImageUrl = getString("itemImageUrl") ?: "",
                deletedForUserIds = deletedList,
                unreadByParticipantIds = unreadList,
                lastMessageStatus = lastStatus
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toChatMessage(): Message? {
        if (!exists()) return null
        return try {
            val rawTimestamp = get("timestamp")
            val timestampMillis = when (rawTimestamp) {
                is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                is Number -> rawTimestamp.toLong()
                else -> System.currentTimeMillis()
            }

            val rawStatus = getString("status")
            val readAt = getLong("readAt")
            val deliveredAt = getLong("deliveredAt")
            val status = when {
                readAt != null || rawStatus == MessageStatus.READ.name -> MessageStatus.READ
                deliveredAt != null || rawStatus == MessageStatus.DELIVERED.name -> MessageStatus.DELIVERED
                rawStatus == MessageStatus.FAILED.name -> MessageStatus.FAILED
                else -> MessageStatus.SENT
            }

            Message(
                id = getString("id") ?: id,
                senderId = getString("senderId") ?: "",
                text = getString("text") ?: "",
                timestamp = timestampMillis,
                status = status,
                isLocationPin = getBoolean("isLocationPin") ?: false,
                locationName = getString("locationName"),
                isEdited = getBoolean("isEdited") ?: false,
                isDeleted = getBoolean("isDeleted") ?: false,
                readAt = readAt,
                deliveredAt = deliveredAt
            )
        } catch (e: Exception) {
            null
        }
    }
}
