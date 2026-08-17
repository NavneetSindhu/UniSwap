package com.minimize.uniswap.data.repository.firebase

import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ItemRepository {

    private val itemsCollection = firestore.collection("items")

    override suspend fun getItems(): List<CampusItem> {
        return try {
            itemsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(CampusItem::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Real-time stream of items from Firestore.
     */
    fun getItemsFlow(): Flow<List<CampusItem>> = callbackFlow {
        val subscription = itemsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(CampusItem::class.java)
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun postItem(item: CampusItem): Boolean {
        return try {
            // Add a timestamp for ordering
            val itemWithTimestamp = item.copy(timeAgo = "Just now") // You might want a server timestamp
            itemsCollection.document(item.id).set(itemWithTimestamp).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
