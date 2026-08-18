package com.minimize.uniswap.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extended color tokens for UniSwap sustainability brand.
 */
@Immutable
data class ExtendedColors(
    val wasteMetricGreen: Color,
    val campusAmber: Color,
    val success: Color,
    val surfaceHighlight: Color
)

/**
 * Standardized spacing and dimension tokens.
 */
@Immutable
data class UniSwapDimens(
    val spaceExtraSmall: Dp = 4.dp,
    val spaceSmall: Dp = 8.dp,
    val spaceMedium: Dp = 16.dp,
    val spaceLarge: Dp = 24.dp,
    val spaceExtraLarge: Dp = 32.dp,
    val cardRadius: Dp = 24.dp,
    val buttonRadius: Dp = 16.dp,
    val chipRadius: Dp = 12.dp
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        wasteMetricGreen = Color.Unspecified,
        campusAmber = Color.Unspecified,
        success = Color.Unspecified,
        surfaceHighlight = Color.Unspecified
    )
}

val LocalUniSwapDimens = staticCompositionLocalOf { UniSwapDimens() }
