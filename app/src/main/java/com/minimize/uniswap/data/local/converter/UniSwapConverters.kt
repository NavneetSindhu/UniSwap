package com.minimize.uniswap.data.local.converter

import androidx.room.TypeConverter
import com.minimize.uniswap.data.model.MessageStatus

class UniSwapConverters {
    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMessageStatus(status: String): MessageStatus {
        return MessageStatus.valueOf(status)
    }
}
