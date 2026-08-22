package com.minimize.uniswap.data.model

import com.google.gson.annotations.SerializedName

/**
 * Campus Category data model.
 * Driven dynamically via Firebase Remote Config / DataStore with offline defaults.
 */
data class CampusCategory(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("iconUrl")
    val iconUrl: String = "",
    @SerializedName("order")
    val order: Int = 0
)
