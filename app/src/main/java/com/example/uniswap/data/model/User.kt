package com.example.uniswap.data.model

data class User(
    val id: String,
    val name: String,
    val major: String,
    val year: String,
    val level: String = "Eco-Warrior Level 1",
    val profileImageUrl: String = ""
)