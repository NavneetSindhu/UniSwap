package com.minimize.uniswap.ui.navigation

enum class Screen(val route: String) {
    Splash("splash"),
    Onboarding("onboarding"),
    Login("login"),
    Feed("feed"),
    Details("details/{itemId}"),
    Chat("chat/{itemId}"),
    Sell("sell"),
    Profile("profile"),
    Settings("settings");

    companion object {
        fun createDetailsRoute(itemId: String): String = "details/$itemId"
        fun createChatRoute(itemId: String): String = "chat/$itemId"
    }
}