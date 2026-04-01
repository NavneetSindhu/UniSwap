package com.example.uniswap.data.repository

import com.example.uniswap.data.model.CampusItem
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getAllItems(): Flow<List<CampusItem>>
    fun getItemById(id: String): CampusItem?
    suspend fun postItem(item: CampusItem): Boolean
}