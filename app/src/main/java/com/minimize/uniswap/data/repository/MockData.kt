//package com.minimize.uniswap.data.repository
//
//import com.minimize.uniswap.data.model.CampusItem
//import com.minimize.uniswap.data.model.ItemCategory
//import com.minimize.uniswap.data.model.ItemStatus
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//
///**
// * Singleton Repository that acts as our "In-Memory Database".
// * This ensures that when you post an item in the SellScreen,
// * it immediately appears in the Feed and Profile screens.
// */
//object MockItemRepository : ItemRepository {
//
//    private val _items = MutableStateFlow(listOf(
//        CampusItem(
//            id = "1",
//            title = "Engineering Textbooks",
//            description = "Complete set for 1st-year engineering students. Includes Physics, Chemistry, and Basic Mechanics. Very minimal highlighting.",
//            price = 45.0,
//            category = ItemCategory.ENGINEERING,
//            location = "North Campus",
//            sellerId = "u1",
//            sellerName = "Sarah J.",
//            timeAgo = "2h ago",
//            imageUrl = "https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=400",
//            isVerified = true,
//            status = ItemStatus.AVAILABLE
//        ),
//        CampusItem(
//            id = "2",
//            title = "Modern Dorm Desk Lamp",
//            description = "Flexible neck with 3 brightness levels. Includes a warm-white LED bulb. Perfect for late-night study sessions without waking up roommates.",
//            price = 0.0,
//            category = ItemCategory.DORM_ESSENTIALS,
//            location = "Library Commons",
//            sellerId = "u2",
//            sellerName = "Alex M.",
//            timeAgo = "15m ago",
//            imageUrl = "https://images.unsplash.com/photo-1534073828943-f801091bb18c?q=80&w=400",
//            isVerified = false,
//            status = ItemStatus.PENDING // Shows the "Claimed/Pending" state in the UI
//        ),
//        CampusItem(
//            id = "3",
//            title = "27\" Dell UltraSharp Monitor",
//            description = "4K resolution with incredible color accuracy. Used for graphic design project last semester. Comes with HDMI and Power cable.",
//            price = 120.0,
//            category = ItemCategory.ELECTRONICS,
//            location = "Eng Block B",
//            sellerId = "me_123", // Your ID
//            sellerName = "Navneet S.",
//            timeAgo = "1h ago",
//            imageUrl = "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?q=80&w=400",
//            isVerified = true,
//            status = ItemStatus.AVAILABLE
//        ),
//        CampusItem(
//            id = "4",
//            title = "Architecture Drafting Set",
//            description = "Professional T-Square, set squares, and a portable drafting board. Perfect for 2nd-year architecture studios.",
//            price = 35.0,
//            category = ItemCategory.ARCHITECTURE,
//            location = "West Dorms",
//            sellerId = "u1",
//            sellerName = "Sarah J.",
//            timeAgo = "5h ago",
//            imageUrl = "https://images.unsplash.com/photo-1503387762-592dea58ef21?q=80&w=400",
//            isVerified = true,
//            status = ItemStatus.AVAILABLE
//        ),
//        CampusItem(
//            id = "5",
//            title = "Compact Mini Fridge",
//            description = "Fits perfectly under standard dorm desks. Energy-efficient and quiet. Cleaned and defrosted, ready for pick up.",
//            price = 50.0,
//            category = ItemCategory.DORM_ESSENTIALS,
//            location = "South Hall",
//            sellerId = "u3",
//            sellerName = "Kevin L.",
//            timeAgo = "Just now",
//            imageUrl = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?q=80&w=400",
//            isVerified = false,
//            status = ItemStatus.AVAILABLE
//        )
//    ))
//
//    override fun getAllItems(): Flow<List<CampusItem>> = _items.asStateFlow()
//
//    override fun getItemById(id: String): CampusItem? = _items.value.find { it.id == id }
//
//    override suspend fun postItem(item: CampusItem): Boolean {
//        // Simulating a real network delay for that "Spring Boot" feel later
//        delay(1000)
//        _items.update { currentList -> listOf(item) + currentList }
//        return true
//    }
//}
