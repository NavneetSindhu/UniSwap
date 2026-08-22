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
                                doc.toObject(Message::class.java)?.copy(id = doc.id)
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

    override suspend fun sendMessage(itemId: String, buyerId: String, sellerId: String, message: Message): Boolean {
        val chatId = getChatId(itemId, buyerId, sellerId)
        val messageId = message.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val messageToSend = message.copy(id = messageId)
        val entity = messageToSend.toEntity(chatId).copy(status = MessageStatus.SENDING)
        
        // 1. Optimistic update to Room
        messageDao.insertMessage(entity)

        return try {
            // 2. Prepare Firestore data with server timestamp and matching ID
            val messageData = hashMapOf(
                "id" to messageId,
                "senderId" to messageToSend.senderId,
                "text" to messageToSend.text,
                "timestamp" to messageToSend.timestamp,
                "firestoreTimestamp" to FieldValue.serverTimestamp(),
                "isLocationPin" to messageToSend.isLocationPin,
                "locationName" to messageToSend.locationName
            )

            // 3. Send to Firestore using the exact same messageId as document key
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(messageData)
                .await()

            // 4. Update Room status to SENT
            messageDao.updateMessageStatus(messageId, MessageStatus.SENT)
            
            true
        } catch (e: Exception) {
            // 5. Update Room status to FAILED
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
}
