package com.minimize.uniswap.ui

import android.util.Log // Added Log import
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.minimize.uniswap.ui.components.CustomBottomNav
import com.minimize.uniswap.ui.navigation.Screen
import com.minimize.uniswap.ui.screens.auth.LoginScreen
import com.minimize.uniswap.ui.screens.auth.SignupScreen
import com.minimize.uniswap.ui.screens.chat.PickupChatScreen
import com.minimize.uniswap.ui.screens.details.ItemDetailsScreen
import com.minimize.uniswap.ui.screens.feed.CampusFeedScreen
import com.minimize.uniswap.ui.screens.profile.ProfileScreen
import com.minimize.uniswap.ui.screens.sell.SellScreen

private const val TAG = "LOGCAT_NAV"

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn = remember { viewModel.isUserLoggedIn() }

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
            startDestination = if (isLoggedIn) Screen.Feed.route else "login",
            modifier = Modifier.padding(if (showBottomNav) innerPadding else PaddingValues(0.dp)),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 1000 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -1000 }) }
        ) {

            // --- AUTHENTICATION SECTION ---

            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        Log.d(TAG, "onLoginSuccess triggered! Navigating to: ${Screen.Feed.route}")
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = {
                        Log.d(TAG, "Navigating to Signup")
                        navController.navigate("signup")
                    }
                )
            }

            composable("signup") {
                SignupScreen(
                    onSignupSuccess = {
                        Log.d(TAG, "onSignupSuccess triggered! Navigating to: ${Screen.Feed.route}")
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate("login") }
                )
            }

            // --- APP SECTION ---
            composable(Screen.Feed.route) {
                Log.d(TAG, "CampusFeedScreen is now active.")
                CampusFeedScreen(onItemClick = { item ->
                    navController.navigate(Screen.Details.createRoute(item.id))
                })
            }

            // Item Details
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

            // Pickup Chat
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

            // Sell Screen
            composable(Screen.Sell.route) {
                SellScreen(onPostSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Sell.route) { inclusive = true }
                    }
                })
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
