package com.minimize.uniswap.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.ui.navigation.Screen
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Floating glassmorphic bottom navigation bar with central Create Item button.
 * Container height: 62dp, Corner radius: 50 (pill shape).
 * Sliding highlighter circle size: 62dp (fills container vertically).
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CustomBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    hasUnreadMessages: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 1. Core tabs with Center Create Item tab
    val tabs = listOf(
        Screen.Home.route,
        Screen.Feed.route,
        Screen.Sell.route,
        Screen.Messages.route,
        Screen.Profile.route
    )
    val icons = listOf(
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

            // Full edge overlap on extreme ends:
            // Index 0 -> 0.dp (fully flushes with left rounded border)
            // Index (N-1) -> (maxWidth - indicatorSize) (fully flushes with right rounded border)
            val indicatorOffset by animateDpAsState(
                targetValue = step * selectedIndex,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
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
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    label = "icon_tint"
                )

                Box(
                    modifier = Modifier
                        .offset(x = tabXOffset, y = 0.dp)
                        .size(indicatorSize)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Clean ripple-free iOS-like feel
                            onClick = { onNavigate(route) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = route,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )

                        // Vibrant Red indicator dot for unread messages
                        if (route == Screen.Messages.route && hasUnreadMessages) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                        }
                    }
                }
            }
        }
    }
}