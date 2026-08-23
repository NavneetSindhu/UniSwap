package com.minimize.uniswap.ui.navigation

import android.net.Uri

enum class Screen(val route: String) {
    Splash("splash"),
    Onboarding("onboarding"),
    Login("login"),
    Feed("feed"),
    Details("details/{itemId}"),
    Chat("chat/{itemId}?initialMessage={initialMessage}&buyerId={buyerId}"),
    Sell("sell"),
    Profile("profile"),
    Home("home"),
    Messages("messages"),
    Settings("settings"),
    SignUp("signup");

    companion object {
        fun createDetailsRoute(itemId: String): String = "details/$itemId"
        fun createChatRoute(itemId: String, initialMessage: String? = null, buyerId: String? = null): String {
            val encodedMsg = if (!initialMessage.isNullOrBlank()) Uri.encode(initialMessage) else ""
            val encodedBuyer = if (!buyerId.isNullOrBlank()) Uri.encode(buyerId) else ""
            return "chat/$itemId?initialMessage=$encodedMsg&buyerId=$encodedBuyer"
        }
    }
}
