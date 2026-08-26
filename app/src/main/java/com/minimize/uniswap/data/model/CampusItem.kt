package com.minimize.uniswap.data.model

enum class ItemCategory {
    ARCHITECTURE, DORM_ESSENTIALS, ENGINEERING, ELECTRONICS, OTHER;

    fun getPlaceholderUrl(): String {
        return when (this) {
            ARCHITECTURE -> "https://images.unsplash.com/photo-1503387762-592dea58ef21?q=80&w=800"
            DORM_ESSENTIALS -> "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?q=80&w=800"
            ENGINEERING -> "https://images.unsplash.com/photo-1581094794329-c8112a89af12?q=80&w=800"
            ELECTRONICS -> "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?q=80&w=800"
            OTHER -> "https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=800"
        }
    }
}

enum class ItemStatus {
    AVAILABLE, PENDING, SOLD
}

data class CampusItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val isFree: Boolean = false,
    val category: ItemCategory = ItemCategory.OTHER,
    val location: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val timeAgo: String = "",
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val status: ItemStatus = ItemStatus.AVAILABLE,
    val sellerAvatarId: String = "avatar_scholar",
    val timestamp: Long = System.currentTimeMillis(),
    val viewsCount: Int = 0,
    val favoritesCount: Int = 0,
    val condition: String = "Good",
    val campusCenter: String = "",
    val customCategory: String = ""
) {
    fun getDisplayCategory(): String {
        return if (customCategory.isNotBlank()) {
            customCategory
        } else {
            category.name.replace("_", " ")
                .lowercase(java.util.Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        }
    }
    fun getAllImages(): List<String> {
        val list = imageUrls.filter { it.isNotBlank() }
        if (list.isNotEmpty()) return list
        if (imageUrl.isNotBlank()) return listOf(imageUrl)
        return listOf(category.getPlaceholderUrl())
    }

    /**
     * Calculates time-decaying engagement score for Trending feed algorithm.
     */
    fun calculateTrendingScore(): Double {
        val hoursElapsed = maxOf(0.1, (System.currentTimeMillis() - timestamp) / (1000.0 * 60.0 * 60.0))
        val engagement = (viewsCount * 1.0) + (favoritesCount * 3.0) + (if (isVerified) 5.0 else 0.0)
        return (engagement + 1.0) / Math.pow(hoursElapsed + 2.0, 1.2)
    }
}
