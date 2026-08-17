package com.minimize.uniswap.data.repository.firebase

import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ItemRepository {

    private val itemsCollection = firestore.collection("items")

    override suspend fun getItems(): List<CampusItem> {
        return try {
            itemsCollection.get().await().toObjects(CampusItem::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun postItem(item: CampusItem): Boolean {
        return try {
            itemsCollection.document(item.id).set(item).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
