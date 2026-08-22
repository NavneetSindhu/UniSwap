package com.minimize.uniswap.ui.navigation

import android.net.Uri

enum class Screen(val route: String) {
    Splash("splash"),
    Onboarding("onboarding"),
    Login("login"),
    Feed("feed"),
    Details("details/{itemId}"),
    Chat("chat/{itemId}?initialMessage={initialMessage}"),
    Sell("sell"),
    Profile("profile"),
    Home("home"),
    Messages("messages"),
    Settings("settings"),
    SignUp("signup");

    companion object {
        fun createDetailsRoute(itemId: String): String = "details/$itemId"
        fun createChatRoute(itemId: String, initialMessage: String? = null): String {
            return if (!initialMessage.isNullOrBlank()) {
                val encoded = Uri.encode(initialMessage)
                "chat/$itemId?initialMessage=$encoded"
            } else {
                "chat/$itemId?initialMessage="
            }
        }
    }
}
