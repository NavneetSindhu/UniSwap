package com.minimize.uniswap.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Modifier extension that applies an infinite animated diagonal shimmer gradient.
 * Automatically adapts to dark and light mode palette tokens.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(0.dp),
    customBaseColor: Color? = null,
    customHighlightColor: Color? = null
): Modifier = composed {
    val isDark = UniSwapTheme.colors.isDark

    val baseColor = customBaseColor ?: if (isDark) {
        Color(0xFF1E2126)
    } else {
        Color(0xFFE8ECEF)
    }

    val highlightColor = customHighlightColor ?: if (isDark) {
        Color(0xFF2E333D)
    } else {
        Color(0xFFF6F8FA)
    }

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = -800f,
        targetValue = 2200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate_anim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor,
            highlightColor,
            baseColor,
            baseColor
        ),
        start = Offset(translateAnimation - 800f, translateAnimation - 800f),
        end = Offset(translateAnimation, translateAnimation)
    )

    this
        .clip(shape)
        .background(brush)
}

/**
 * Reusable rectangular shimmer skeleton element.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    customBaseColor: Color? = null,
    customHighlightColor: Color? = null
) {
    Box(
        modifier = modifier.shimmerEffect(
            shape = shape,
            customBaseColor = customBaseColor,
            customHighlightColor = customHighlightColor
        )
    )
}

/**
 * Reusable circular shimmer skeleton element (for avatars, icon badges).
 */
@Composable
fun ShimmerCircle(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    customBaseColor: Color? = null,
    customHighlightColor: Color? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .shimmerEffect(
                shape = CircleShape,
                customBaseColor = customBaseColor,
                customHighlightColor = customHighlightColor
            )
    )
}

/**
 * Reusable pill-shaped shimmer skeleton element (for tags, category chips, buttons).
 */
@Composable
fun ShimmerPill(
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 32.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    customBaseColor: Color? = null,
    customHighlightColor: Color? = null
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .shimmerEffect(
                shape = shape,
                customBaseColor = customBaseColor,
                customHighlightColor = customHighlightColor
            )
    )
}
