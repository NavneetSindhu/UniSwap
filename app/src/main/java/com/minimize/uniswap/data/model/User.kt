package com.minimize.uniswap.data.model

/**
 * Domain model for UniSwap User.
 * Stored in Firestore users collection.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val isEmailVerified: Boolean = false,
    val lbsSaved: Double = 0.0,
    val itemsRecycled: Int = 0,
    val campusCenter: String = "Main Campus",
    val profilePicUrl: String = ""
)
