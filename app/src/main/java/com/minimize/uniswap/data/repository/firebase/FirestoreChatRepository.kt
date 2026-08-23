package com.minimize.uniswap.data.repository.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.minimize.uniswap.data.local.dao.MessageDao
import com.minimize.uniswap.data.local.entity.MessageEntity
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

import com.google.firebase.firestore.SetOptions
import com.minimize.uniswap.data.model.ChatThread

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
        return "${itemId}_${uids[0]}_${uids[1]}"
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
                    }.sortedByDescending { it.lastMessageTimestamp }
                    trySend(threads)
                }
            }
        awaitClose { subscription.remove() }
    }

    private fun startSyncListener(chatId: String) {
        val chatRef = firestore.collection("chats").document(chatId).collection("messages")
        
        // This listener runs in the applicationScope to ensure it keeps Room updated
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
            // 2. Update/Create parent chat thread metadata document
            val chatMetadata = hashMapOf<String, Any>(
                "id" to chatId,
                "itemId" to itemId,
                "buyerId" to buyerId,
                "sellerId" to sellerId,
                "participants" to listOf(buyerId, sellerId),
                "lastMessage" to messageToSend.text,
                "lastMessageTimestamp" to messageToSend.timestamp,
                "lastSenderId" to messageToSend.senderId
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
                "locationName" to messageToSend.locationName
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

    // Extension functions for mapping
    private fun MessageEntity.toDomainModel() = Message(
        id = id,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = status,
        isLocationPin = isLocationPin,
        locationName = locationName
    )

    private fun Message.toEntity(chatId: String) = MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = status,
        isLocationPin = isLocationPin,
        locationName = locationName
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
                itemImageUrl = getString("itemImageUrl") ?: ""
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

            Message(
                id = getString("id") ?: id,
                senderId = getString("senderId") ?: "",
                text = getString("text") ?: "",
                timestamp = timestampMillis,
                status = MessageStatus.SENT,
                isLocationPin = getBoolean("isLocationPin") ?: false,
                locationName = getString("locationName")
            )
        } catch (e: Exception) {
            null
        }
    }
}
