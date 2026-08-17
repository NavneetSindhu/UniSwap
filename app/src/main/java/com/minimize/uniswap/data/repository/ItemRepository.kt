package com.minimize.uniswap.data.repository

import com.minimize.uniswap.data.model.CampusItem
import kotlinx.coroutines.flow.Flow

/**
 * The bridge between the ViewModel and the Data Source (Network or Mock).
 */
interface ItemRepository {
    // Used by the Home screen to show all items
    suspend fun getItems(): List<CampusItem>

    // Real-time stream for the feed
    fun getItemsFlow(): Flow<List<CampusItem>>

    // Used by the Sell screen to send a new item to the server
    // Returns TRUE if the server says "OK", FALSE if it fails
    suspend fun postItem(item: CampusItem): Boolean
}
