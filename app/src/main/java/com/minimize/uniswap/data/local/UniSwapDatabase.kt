package com.minimize.uniswap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minimize.uniswap.data.model.CampusItem

@Database(entities = [CampusItem::class], version = 2, exportSchema = false)
abstract class UniSwapDatabase : RoomDatabase() {
    // DAO will be added in Commit 4
}
