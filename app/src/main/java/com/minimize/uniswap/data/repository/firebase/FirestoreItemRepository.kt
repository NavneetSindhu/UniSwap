package com.minimize.uniswap.data.repository.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
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
            val snapshot = itemsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toCampusItem() }
                .filter { it.status == ItemStatus.AVAILABLE }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getItemsFlow(): Flow<List<CampusItem>> = callbackFlow {
        val subscription = itemsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toCampusItem() }
                        .filter { it.status == ItemStatus.AVAILABLE }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getItemsBySellerFlow(sellerId: String): Flow<List<CampusItem>> = callbackFlow {
        val subscription = itemsCollection
            .whereEqualTo("sellerId", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toCampusItem() }
                        .sortedByDescending { it.timestamp }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun postItem(item: CampusItem): Boolean {
        return try {
            val docRef = itemsCollection.document(item.id)
            val itemWithTimestamp = if (item.timestamp == 0L) {
                item.copy(timestamp = System.currentTimeMillis())
            } else {
                item
            }
            docRef.set(itemWithTimestamp).await()
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
                    val item = snapshot.toCampusItem()
                    trySend(item)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun fetchItemById(itemId: String) {
        // Handled reactively by getItemByIdFlow
    }

    override suspend fun updateItemStatus(itemId: String, status: ItemStatus): Boolean {
        return try {
            itemsCollection.document(itemId)
                .update("status", status.name)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteItem(itemId: String): Boolean {
        return try {
            itemsCollection.document(itemId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun DocumentSnapshot.toCampusItem(): CampusItem? {
        if (!exists()) return null
        return try {
            val rawTimestamp = get("timestamp")
            val timestampMillis = when (rawTimestamp) {
                is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                is Number -> rawTimestamp.toLong()
                else -> System.currentTimeMillis()
            }

            val statusStr = getString("status") ?: ItemStatus.AVAILABLE.name
            val status = try {
                ItemStatus.valueOf(statusStr)
            } catch (e: Exception) {
                ItemStatus.AVAILABLE
            }

            val categoryStr = getString("category") ?: ItemCategory.OTHER.name
            val category = try {
                ItemCategory.valueOf(categoryStr)
            } catch (e: Exception) {
                ItemCategory.OTHER
            }

            val rawImageUrls = (get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            CampusItem(
                id = getString("id") ?: id,
                title = getString("title") ?: "",
                description = getString("description") ?: "",
                price = getDouble("price") ?: (get("price") as? Number)?.toDouble() ?: 0.0,
                isFree = getBoolean("isFree") ?: false,
                category = category,
                location = getString("location") ?: "Campus",
                sellerId = getString("sellerId") ?: "",
                sellerName = getString("sellerName") ?: "Campus User",
                timeAgo = getString("timeAgo") ?: "Just now",
                imageUrl = getString("imageUrl") ?: "",
                imageUrls = rawImageUrls,
                isVerified = getBoolean("isVerified") ?: false,
                status = status,
                timestamp = timestampMillis
            )
        } catch (e: Exception) {
            null
        }
    }
}
