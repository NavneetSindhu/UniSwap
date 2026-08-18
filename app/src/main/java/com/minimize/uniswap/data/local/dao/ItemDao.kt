package com.minimize.uniswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minimize.uniswap.data.model.CampusItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<CampusItem>>

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    fun getItemById(itemId: String): Flow<CampusItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CampusItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CampusItem>)

    @Query("DELETE FROM items")
    suspend fun clearAll()

    @Query("DELETE FROM items WHERE status = 'SOLD'")
    suspend fun clearSoldItems()
}