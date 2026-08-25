package com.minimize.uniswap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minimize.uniswap.data.local.converter.UniSwapConverters
import com.minimize.uniswap.data.local.dao.MessageDao
import com.minimize.uniswap.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 5, exportSchema = false)
@TypeConverters(UniSwapConverters::class)
abstract class UniSwapDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
