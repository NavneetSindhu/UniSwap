package com.minimize.uniswap.ui

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.minimize.uniswap.ui.screens.settings.SettingsScreen

private const val TAG = "LOGCAT_NAV"

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var authCheckComplete by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Screen.Login.route) }

    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.isUserLoggedIn()
        startDestination = if (isLoggedIn) Screen.Feed.route else Screen.Login.route
        authCheckComplete = true
    }

    // Bottom navigation visible only on core top-level tabs
    val showBottomNav = currentRoute in listOf(
        Screen.Feed.route,
        Screen.Profile.route,
        Screen.Sell.route
    )

    if (!authCheckComplete) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomNav) {
                Box(modifier = Modifier.navigationBarsPadding()) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else innerPadding.calculateBottomPadding()),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 1000 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -1000 }) }
        ) {

            // --- AUTHENTICATION ---
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        Log.d(TAG, "onLoginSuccess triggered! Navigating to: ${Screen.Feed.route}")
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    onSignupSuccess = {
                        Log.d(TAG, "onSignupSuccess triggered! Navigating to: ${Screen.Feed.route}")
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            // --- APP CORE TABS ---
            composable(Screen.Feed.route) {
                CampusFeedScreen(onItemClick = { item ->
                    navController.navigate(Screen.createDetailsRoute(item.id))
                })
            }

            composable(Screen.Sell.route) {
                SellScreen(onPostSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Sell.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }

            // --- SUB-SCREENS ---
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
//                        viewModel.logout()
//                        navController.navigate(Screen.Login.route) {
//                            popUpTo(0) { inclusive = true }
//                        }
                    }
                )
            }

            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                ItemDetailsScreen(
                    itemId = itemId,
                    onBackClick = { navController.popBackStack() },
                    onClaimClick = { navController.navigate(Screen.createChatRoute(itemId)) }
                )
            }

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
        }
    }
}