package com.minimize.uniswap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minimize.uniswap.data.local.dao.ItemDao
import com.minimize.uniswap.data.model.CampusItem

@Database(entities = [CampusItem::class], version = 1, exportSchema = false)
abstract class UniSwapDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
