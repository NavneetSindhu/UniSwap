package com.minimize.uniswap.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extended color tokens for UniSwap design system that react to Light/Dark/Accent themes.
 */
@Immutable
data class ExtendedColors(
    val isDark: Boolean,
    val background: Color,
    val cardSurface: Color,
    val cardSurfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textSubtle: Color,
    val btnBackBg: Color,
    val glassNavStart: Color,
    val glassNavEnd: Color,
    val navIndicatorBg: Color,
    val navIndicatorIconTint: Color,
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
        isDark = true,
        background = PaletteDark.Base,
        cardSurface = CardDarkSurface,
        cardSurfaceVariant = PaletteDark.Gray200,
        textPrimary = Color.White,
        textSecondary = TextMutedLight,
        textSubtle = TextSubtle,
        btnBackBg = BtnBackBg,
        glassNavStart = GlassNavStart,
        glassNavEnd = GlassNavEnd,
        navIndicatorBg = NavIndicatorBg,
        navIndicatorIconTint = PaletteLight.Gray950,
        wasteMetricGreen = WasteMetricGreen,
        campusAmber = CampusAmber,
        success = SuccessGreen,
        surfaceHighlight = Color(0xFF2C2C2C)
    )
}

val LocalUniSwapDimens = staticCompositionLocalOf { UniSwapDimens() }
