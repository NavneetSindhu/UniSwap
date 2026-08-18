package com.minimize.uniswap.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.minimize.uniswap.data.model.Message
import com.minimize.uniswap.data.repository.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private fun getChatId(itemId: String, buyerId: String, sellerId: String): String {
        return "${itemId}_${buyerId}_${sellerId}"
    }

    override fun getMessages(itemId: String, buyerId: String, sellerId: String): Flow<List<Message>> = callbackFlow {
        val chatId = getChatId(itemId, buyerId, sellerId)
        val subscription = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(messages)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendMessage(itemId: String, buyerId: String, sellerId: String, message: Message): Boolean {
        return try {
            val chatId = getChatId(itemId, buyerId, sellerId)
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}