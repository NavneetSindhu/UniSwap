package com.example.uniswap.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.uniswap.ui.components.CustomBottomNav
import com.example.uniswap.ui.navigation.Screen
import com.example.uniswap.ui.screens.auth.LoginScreen
import com.example.uniswap.ui.screens.chat.PickupChatScreen
import com.example.uniswap.ui.screens.details.ItemDetailsScreen
import com.example.uniswap.ui.screens.feed.CampusFeedScreen
import com.example.uniswap.ui.screens.profile.ProfileScreen
import com.example.uniswap.ui.screens.sell.SellScreen
import com.example.uniswap.ui.screens.auth.SignupScreen // Import your new screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logic to hide bottom nav on sub-screens and AUTH screens
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
            // 1. Set Start Destination to Signup (or Login)
            startDestination = "login",
            modifier = Modifier.padding(if (showBottomNav) innerPadding else PaddingValues(0.dp)),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 1000 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -1000 }) }
        ) {

            // --- AUTHENTICATION SECTION ---


            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToSignup = { navController.navigate("signup") }
                )
            }

            composable("signup") {
                SignupScreen(onSignupSuccess = {
                    // After signup, navigate to Feed and clear the auth stack
                    navController.navigate(Screen.Feed.route) {
                        popUpTo("signup") { inclusive = true }
                    }
                })
            }

            // --- APP SECTION ---

            // 1. Campus Feed
            composable(Screen.Feed.route) {
                CampusFeedScreen(onItemClick = { item ->
                    navController.navigate(Screen.Details.createRoute(item.id))
                })
            }

            // 2. Item Details
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
                PickupChatScreen(
                    itemId = itemId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4. Sell Screen
            composable(Screen.Sell.route) {
                SellScreen(onPostSuccess = {
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