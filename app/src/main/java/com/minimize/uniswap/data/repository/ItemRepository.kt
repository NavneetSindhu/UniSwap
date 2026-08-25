package com.minimize.uniswap.data.repository

import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemStatus
import kotlinx.coroutines.flow.Flow

/**
 * The bridge between the ViewModel and the Data Source (Network or Mock).
 */
// In ItemRepository.kt
interface ItemRepository {
    suspend fun getItems(): List<CampusItem>
    fun getItemsFlow(): Flow<List<CampusItem>>
    fun getItemByIdFlow(itemId: String): Flow<CampusItem?>
    fun getItemsBySellerFlow(sellerId: String): Flow<List<CampusItem>>
    suspend fun fetchItemById(itemId: String)
    suspend fun postItem(item: CampusItem): Boolean
    suspend fun updateItemStatus(itemId: String, status: ItemStatus): Boolean
    suspend fun deleteItem(itemId: String): Boolean

    /**
     * Toggles whether an item is saved by the current authenticated user.
     * @return Result containing true if item is now saved, false if unsaved.
     */
    suspend fun toggleSaveItem(itemId: String): Result<Boolean>

    /**
     * Real-time stream of all item IDs saved by the current user.
     */
    fun getSavedItemIdsFlow(): Flow<Set<String>>

    /**
     * Real-time stream of full CampusItem objects saved by the current user.
     */
    fun getSavedItemsFlow(): Flow<List<CampusItem>>
}
