package com.minimize.uniswap.data.repository.firebase

import com.minimize.uniswap.data.local.dao.ItemDao
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val itemDao: ItemDao,
    private val applicationScope: CoroutineScope
) : ItemRepository {

    private val itemsCollection = firestore.collection("items")

    override suspend fun getItems(): List<CampusItem> {
        return try {
            val items = itemsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(CampusItem::class.java)
            
            // Sync with local cache
            itemDao.clearAll()
            itemDao.insertItems(items)
            
            items
        } catch (e: Exception) {
            // If network fails, return cached items (first item in the flow)
            itemDao.getAllItems().first()
        }
    }

    /**
     * Real-time stream: Drives UI from Room, while Firestore updates Room.
     */
    override fun getItemsFlow(): Flow<List<CampusItem>> {
        // Start the Firestore listener to update Room
        val firestoreFlow = callbackFlow {
            val subscription = itemsCollection
                .whereEqualTo("status", ItemStatus.AVAILABLE.name) // Filter at source
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

        // Return the Room flow as the source of truth
        return itemDao.getAllItems().onStart {
            // Trigger a background sync when the flow starts
            firestoreFlow.onEach { items ->
                itemDao.clearAll()
                itemDao.insertItems(items)
            }.launchIn(applicationScope) // Using injected application scope
        }
    }

    override suspend fun postItem(item: CampusItem): Boolean {
        return try {
            itemsCollection.document(item.id).set(item).await()
            // Room will be updated automatically by the SnapshotListener in getItemsFlow
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun getItemByIdFlow(itemId: String): Flow<CampusItem?> {
        return itemDao.getItemById(itemId)
    }

    override suspend fun fetchItemById(itemId: String) {
        try {
            val snapshot = itemsCollection.document(itemId).get().await()
            val item = snapshot.toObject(CampusItem::class.java)
            if (item != null) {
                itemDao.insertItem(item)
            }
        } catch (_: Exception) {
            // Local Room cache will still serve whatever was already cached
        }
    }
}
