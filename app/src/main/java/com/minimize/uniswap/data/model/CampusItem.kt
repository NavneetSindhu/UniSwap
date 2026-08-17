package com.minimize.uniswap.data.model

import java.util.UUID

// Using an Enum makes it easier for your Spring Boot backend to validate categories
enum class ItemCategory {
    ARCHITECTURE, DORM_ESSENTIALS, ENGINEERING, ELECTRONICS, OTHER
}

data class CampusItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val price: Double,
    val isFree: Boolean = price == 0.0,
    val category: ItemCategory,
    val location: String,
    val sellerId: String,
    val sellerName: String,
    val timeAgo: String,
    val imageUrl: String,
    val isVerified: Boolean = false,
    val status: ItemStatus = ItemStatus.AVAILABLE
)

enum class ItemStatus {
    AVAILABLE, PENDING, SOLD
}
