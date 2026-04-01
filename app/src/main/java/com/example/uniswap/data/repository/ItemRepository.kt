package com.example.uniswap.data.repository

import com.example.uniswap.data.model.CampusItem

/**
 * The bridge between the ViewModel and the Data Source (Network or Mock).
 */
interface ItemRepository {
    // Used by the Home screen to show all items
    suspend fun getItems(): List<CampusItem>

    // Used by the Sell screen to send a new item to the server
    // Returns TRUE if the server says "OK", FALSE if it fails
    suspend fun postItem(item: CampusItem): Boolean
}