package com.minimize.uniswap.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.minimize.uniswap.ui.screens.chat.PickupChatScreen
import com.minimize.uniswap.ui.screens.details.ItemDetailsScreen
import com.minimize.uniswap.ui.screens.feed.CampusFeedScreen
import com.minimize.uniswap.ui.screens.home.HomeScreen
import com.minimize.uniswap.ui.screens.splash.OnboardingScreen
import com.minimize.uniswap.ui.screens.profile.ProfileScreen
import com.minimize.uniswap.ui.screens.list.ListProductScreen
import com.minimize.uniswap.ui.screens.messages.MessagesScreen
import com.minimize.uniswap.ui.screens.settings.SettingsScreen
import com.minimize.uniswap.ui.screens.splash.SplashScreen
import com.minimize.uniswap.ui.theme.UniSwapTheme
import timber.log.Timber

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hasUnreadMessages by viewModel.hasUnreadMessages.collectAsState()

    val showBottomNav = currentRoute in listOf(
        Screen.Home.route,
        Screen.Feed.route,
        Screen.Messages.route,
        Screen.Profile.route
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UniSwapTheme.colors.background)
    ) {
        val startDestination = remember {
            if (viewModel.isUserLoggedIn()) Screen.Home.route else Screen.Onboarding.route
        }

        val bottomTabs = remember {
            listOf(
                Screen.Home.route,
                Screen.Feed.route,
                Screen.Sell.route,
                Screen.Messages.route,
                Screen.Profile.route
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val initialRoute = initialState.destination.route
                val targetRoute = targetState.destination.route
                val initialIndex = bottomTabs.indexOf(initialRoute)
                val targetIndex = bottomTabs.indexOf(targetRoute)

                val direction = if (initialIndex != -1 && targetIndex != -1) {
                    if (targetIndex > initialIndex) AnimatedContentTransitionScope.SlideDirection.Left
                    else AnimatedContentTransitionScope.SlideDirection.Right
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Left
                }

                slideIntoContainer(
                    towards = direction,
                    initialOffset = { it / 6 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                val initialRoute = initialState.destination.route
                val targetRoute = targetState.destination.route
                val initialIndex = bottomTabs.indexOf(initialRoute)
                val targetIndex = bottomTabs.indexOf(targetRoute)

                val direction = if (initialIndex != -1 && targetIndex != -1) {
                    if (targetIndex > initialIndex) AnimatedContentTransitionScope.SlideDirection.Left
                    else AnimatedContentTransitionScope.SlideDirection.Right
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Left
                }

                slideOutOfContainer(
                    towards = direction,
                    targetOffset = { it / 6 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 0.98f, animationSpec = tween(180, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    initialOffset = { it / 6 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    targetOffset = { it / 6 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 0.98f, animationSpec = tween(180, easing = FastOutSlowInEasing))
            }
        ) {

            // --- ONBOARDING ---
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }

            // --- AUTHENTICATION ---
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        Timber.d("onLoginSuccess triggered! Navigating to: %s", Screen.Home.route)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }

            composable(Screen.SignUp.route) {
                com.minimize.uniswap.ui.screens.auth.SignUpScreen(
                    onSignUpSuccess = {
                        Timber.d("onSignUpSuccess triggered! Navigating to: %s", Screen.Home.route)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSignIn = {
                        navController.popBackStack()
                    }
                )
            }

            // --- APP CORE TABS ---
            composable(Screen.Feed.route) {
                CampusFeedScreen(
                    onItemClick = { item ->
                        navController.navigate(Screen.createDetailsRoute(item.id))
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onItemClick = { item ->
                        navController.navigate(Screen.createDetailsRoute(item.id))
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSeeAllClick = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Sell.route) {
                ListProductScreen(
                    onPostSuccess = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Sell.route) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onItemClick = { item ->
                        navController.navigate(Screen.createDetailsRoute(item.id))
                    }
                )
            }

            // --- SUB-SCREENS ---
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
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
                    onChatClick = { targetItemId ->
                        navController.navigate(Screen.createChatRoute(targetItemId))
                    },
                    onOfferClick = { targetItemId, defaultMsg ->
                        navController.navigate(Screen.createChatRoute(targetItemId, defaultMsg))
                    }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                    navArgument("initialMessage") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    },
                    navArgument("buyerId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                val rawInitialMessage = backStackEntry.arguments?.getString("initialMessage")?.takeIf { it.isNotBlank() }
                val initialMessage = rawInitialMessage?.let { android.net.Uri.decode(it) }?.takeIf { it.isNotBlank() }
                val rawBuyerId = backStackEntry.arguments?.getString("buyerId")?.takeIf { it.isNotBlank() }
                val buyerId = rawBuyerId?.let { android.net.Uri.decode(it) }?.takeIf { it.isNotBlank() }
                PickupChatScreen(
                    itemId = itemId,
                    initialMessage = initialMessage,
                    buyerId = buyerId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(route = Screen.Messages.route) {
                MessagesScreen(
                    onConversationClick = { targetItemId, targetBuyerId ->
                        navController.navigate(Screen.createChatRoute(targetItemId, buyerId = targetBuyerId))
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }

        // Floating Glassmorphic Bottom Navigation Bar Overlay
        AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing))
        ) {
            CustomBottomNav(
                currentRoute = currentRoute ?: Screen.Home.route,
                hasUnreadMessages = hasUnreadMessages,
                onNavigate = { route ->
                    if (route == Screen.Messages.route) {
                        viewModel.markMessagesAsRead()
                    }
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