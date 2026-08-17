package com.minimize.uniswap.ui.navigation

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Details : Screen("details/{itemId}") {
        fun createRoute(itemId: String) = "details/$itemId"
    }
    object Profile : Screen("profile")
    object Sell : Screen("sell")
    object Chat : Screen("chat/{itemId}") {
        fun createRoute(itemId: String) = "chat/$itemId"
    }
}
