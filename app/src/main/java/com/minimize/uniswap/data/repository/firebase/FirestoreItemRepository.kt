package com.minimize.uniswap.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.repository.ItemRepository
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

    override fun getItemsFlow(): Flow<List<CampusItem>> = callbackFlow {
        val subscription = itemsCollection
            .whereEqualTo("status", ItemStatus.AVAILABLE.name)
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
            val docRef = itemsCollection.document(item.id)
            docRef.set(item).await()
            // Update with server timestamp for accurate global ordering
            docRef.update("timestamp", com.google.firebase.Timestamp.now()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getItemByIdFlow(itemId: String): Flow<CampusItem?> = callbackFlow {
        val subscription = itemsCollection.document(itemId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val item = snapshot.toObject(CampusItem::class.java)
                    trySend(item)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun fetchItemById(itemId: String) {
        // No-op in native Firestore refactor as getItemByIdFlow handles it
    }
}
