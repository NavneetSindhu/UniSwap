package com.minimize.uniswap.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.ui.navigation.Screen
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Floating glassmorphic bottom navigation bar with tactile spring physics,
 * filled/outlined icon states, selection bounce micro-interactions, and haptic feedback.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CustomBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    hasUnreadMessages: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Screen.Home.route,
        Screen.Feed.route,
        Screen.Sell.route,
        Screen.Messages.route,
        Screen.Profile.route
    )
    val activeIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.GridView,
        Icons.Default.Add,
        Icons.Filled.ChatBubble,
        Icons.Filled.Person
    )
    val inactiveIcons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.GridView,
        Icons.Default.Add,
        Icons.Outlined.ChatBubbleOutline,
        Icons.Outlined.Person
    )

    val selectedIndex = tabs.indexOf(currentRoute).takeIf { it >= 0 } ?: 0
    val navHeight = 62.dp
    val indicatorSize = 62.dp

    val themeColors = UniSwapTheme.colors
    val haptic = LocalHapticFeedback.current

    // The Floating Glassmorphic Pill Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(navHeight)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        themeColors.glassNavStart.copy(alpha = if (themeColors.isDark) 0.35f else 0.85f),
                        themeColors.glassNavEnd.copy(alpha = if (themeColors.isDark) 0.65f else 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (themeColors.isDark) {
                        listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    } else {
                        listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.03f)
                        )
                    }
                ),
                shape = RoundedCornerShape(50)
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val step = if (tabs.size > 1) (maxWidth - indicatorSize) / (tabs.size - 1) else 0.dp

            // Tactile Spring sliding indicator
            val indicatorOffset by animateDpAsState(
                targetValue = step * selectedIndex,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicator_offset"
            )

            // 2. The Sliding Highlighter Circle (Fills the entire 62dp height and overlaps container edges)
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset, y = 0.dp)
                    .size(indicatorSize)
                    .clip(CircleShape)
                    .background(themeColors.navIndicatorBg)
            )

            // 3. Clickable Tab Icons overlay
            tabs.forEachIndexed { index, route ->
                val isSelected = index == selectedIndex
                val tabXOffset = step * index

                // Smooth icon color transition
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) themeColors.navIndicatorIconTint else if (themeColors.isDark) Color.White.copy(alpha = 0.55f) else com.minimize.uniswap.ui.theme.PaletteLight.Gray600,
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                    label = "icon_tint"
                )

                // Selection scale bounce
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "icon_scale"
                )

                // Optional rotation pop for center add action button
                val iconRotation by animateFloatAsState(
                    targetValue = if (route == Screen.Sell.route && isSelected) 45f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "icon_rotation"
                )

                Box(
                    modifier = Modifier
                        .offset(x = tabXOffset, y = 0.dp)
                        .size(indicatorSize)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Clean ripple-free iOS-like feel
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigate(route)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier.graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            rotationZ = iconRotation
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) activeIcons[index] else inactiveIcons[index],
                            contentDescription = route,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )

                        // Emerald indicator dot for unread messages
                        if (route == Screen.Messages.route && hasUnreadMessages) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.wasteMetricGreen)
                            )
                        }
                    }
                }
            }
        }
    }
}