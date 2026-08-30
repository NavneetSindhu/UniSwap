package com.minimize.uniswap.data.repository.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
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

    override suspend fun toggleSaveItem(itemId: String): Result<Boolean> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be logged in to save items."))
        if (itemId.isBlank()) return Result.failure(IllegalArgumentException("Invalid itemId."))

        val savedDocRef = firestore.collection("users")
            .document(currentUserId)
            .collection("saved_items")
            .document(itemId)

        return try {
            val docSnapshot = savedDocRef.get().await()
            if (docSnapshot.exists()) {
                savedDocRef.delete().await()
                Timber.d("Item %s unsaved by user %s", itemId, currentUserId)
                Result.success(false)
            } else {
                val data = mapOf(
                    "itemId" to itemId,
                    "savedAt" to System.currentTimeMillis()
                )
                savedDocRef.set(data).await()
                Timber.d("Item %s saved by user %s", itemId, currentUserId)
                Result.success(true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle save for item %s: %s", itemId, e.message)
            Result.failure(e)
        }
    }

    override fun getSavedItemIdsFlow(): Flow<Set<String>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            trySend(emptySet())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(currentUserId)
            .collection("saved_items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to saved_items for user %s", currentUserId)
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.mapNotNull { it.id }?.toSet() ?: emptySet()
                trySend(ids)
            }

        awaitClose { listener.remove() }
    }

    override fun getSavedItemsFlow(): Flow<List<CampusItem>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(currentUserId)
            .collection("saved_items")
            .orderBy("savedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to saved_items for user %s", currentUserId)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val savedIds = snapshot?.documents?.mapNotNull { it.id } ?: emptyList()
                if (savedIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                itemsCollection
                    .whereIn(FieldPath.documentId(), savedIds.take(30))
                    .get()
                    .addOnSuccessListener { itemsSnapshot ->
                        val itemsMap = itemsSnapshot.documents.mapNotNull { it.toCampusItem() }.associateBy { it.id }
                        val orderedList = savedIds.mapNotNull { itemsMap[it] }
                        trySend(orderedList)
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error fetching saved item details: %s", e.message)
                    }
            }

        awaitClose { listener.remove() }
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
                sellerAvatarId = getString("sellerAvatarId") ?: "avatar_scholar",
                timestamp = timestampMillis,
                viewsCount = (getLong("viewsCount") ?: (get("viewsCount") as? Number)?.toLong() ?: 0L).toInt(),
                favoritesCount = (getLong("favoritesCount") ?: (get("favoritesCount") as? Number)?.toLong() ?: 0L).toInt(),
                condition = getString("condition") ?: "Good",
                campusCenter = getString("campusCenter") ?: "",
                customCategory = getString("customCategory") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
