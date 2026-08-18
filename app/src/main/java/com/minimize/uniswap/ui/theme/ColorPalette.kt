package com.minimize.uniswap.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Generates a light/dark color scheme based on a primary accent color.
 */
object UniSwapColorPalette {

    fun lightScheme(accentColor: Color): ColorScheme {
        return lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.1f),
            onPrimaryContainer = accentColor,
            secondary = DeepTeal,
            onSecondary = Color.White,
            background = BoneWhite,
            surface = Color.White,
            onSurface = DeepTeal,
            surfaceVariant = LightGrey,
            onSurfaceVariant = Color.Gray,
            error = ErrorRed,
            onError = Color.White
        )
    }

    fun darkScheme(accentColor: Color): ColorScheme {
        // Lighten the accent slightly for dark mode visibility
        val darkAccent = lightenColor(accentColor, 0.2f)
        return darkColorScheme(
            primary = darkAccent,
            onPrimary = Color.Black,
            primaryContainer = darkAccent.copy(alpha = 0.2f),
            onPrimaryContainer = darkAccent,
            secondary = BoneWhite,
            onSecondary = Color.Black,
            background = Onyx,
            surface = Slate,
            onSurface = BoneWhite,
            surfaceVariant = Color(0xFF2C2C2C),
            onSurfaceVariant = Color.LightGray,
            error = Color(0xFFCF6679),
            onError = Color.Black
        )
    }

    private fun lightenColor(color: Color, fraction: Float): Color {
        val argb = color.toArgb()
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(argb, hsl)
        hsl[2] = (hsl[2] + fraction).coerceIn(0f, 1f)
        return Color(ColorUtils.HSLToColor(hsl))
    }
}
