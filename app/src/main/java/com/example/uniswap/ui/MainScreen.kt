package com.example.uniswap.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.uniswap.data.repository.MockItemRepository
import com.example.uniswap.ui.components.CustomBottomNav
import com.example.uniswap.ui.navigation.Screen
import com.example.uniswap.ui.screens.chat.PickupChatScreen
import com.example.uniswap.ui.screens.details.ItemDetailsScreen
import com.example.uniswap.ui.screens.feed.CampusFeedScreen
import com.example.uniswap.ui.screens.profile.ProfileScreen
import com.example.uniswap.ui.screens.sell.SellScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logic to hide bottom nav on sub-screens (Details & Chat)
    // We want these screens to feel immersive and "full-screen"
    val showBottomNav = currentRoute in listOf(
        Screen.Feed.route,
        Screen.Profile.route,
        Screen.Sell.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                CustomBottomNav(
                    currentRoute = currentRoute ?: Screen.Feed.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(if (showBottomNav) innerPadding else androidx.compose.foundation.layout.PaddingValues(0.dp)),
            // Modern Slide + Fade animations
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 1000 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -1000 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -1000 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { 1000 }) }
        ) {
            // 1. Campus Feed
            composable(Screen.Feed.route) {
                CampusFeedScreen(onItemClick = { item ->
                    navController.navigate(Screen.Details.createRoute(item.id))
                })
            }

            // 2. Item Details (Updated to pass itemId only)
            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""

                ItemDetailsScreen(
                    itemId = itemId,
                    onBackClick = { navController.popBackStack() },
                    onClaimClick = { navController.navigate(Screen.Chat.createRoute(itemId)) }
                )
            }

            // 3. Pickup Chat
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""

                // Fetch the item from our repository to pass it into the Chat UI
                val item = MockItemRepository.getItemById(itemId)
                    ?: MockItemRepository.getItemById("1")!! // Fallback

                PickupChatScreen(
                    item = item,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4. Sell Screen
            composable(Screen.Sell.route) {
                SellScreen(onPostSuccess = {
                    // Redirect to feed and clear the sell screen from the backstack
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Sell.route) { inclusive = true }
                    }
                })
            }

            // 5. Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}