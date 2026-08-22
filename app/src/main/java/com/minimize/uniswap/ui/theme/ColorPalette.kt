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

    fun lightScheme(accentColor: Color = UniSwapEmerald): ColorScheme {
        return lightColorScheme(
            primary = accentColor,
            onPrimary = PaletteLight.Base,
            primaryContainer = PaletteLight.Gray100,
            onPrimaryContainer = PaletteLight.Gray950,
            secondary = PaletteLight.Gray800,
            onSecondary = PaletteLight.Base,
            background = PaletteLight.Base,
            onBackground = PaletteLight.Gray950,
            surface = PaletteLight.Base,
            onSurface = PaletteLight.Gray950,
            surfaceVariant = PaletteLight.Gray200,
            onSurfaceVariant = PaletteLight.Gray600,
            outline = PaletteLight.Gray300,
            error = ErrorRed,
            onError = PaletteLight.Base
        )
    }

    fun darkScheme(accentColor: Color = UniSwapEmerald): ColorScheme {
        val darkAccent = lightenColor(accentColor, 0.2f)
        return darkColorScheme(
            primary = PaletteDark.Gray950, // Pure White in dark mode for primary actions (buttons)
            onPrimary = PaletteDark.Base, // Pure Black text on white buttons
            primaryContainer = PaletteDark.Gray200,
            onPrimaryContainer = PaletteDark.Gray950,
            secondary = darkAccent,
            onSecondary = PaletteDark.Base,
            background = PaletteDark.Base, // Pure Black #000000
            onBackground = PaletteDark.Gray950, // Pure White #FAFAFA
            surface = PaletteDark.Gray100, // #171717
            onSurface = PaletteDark.Gray950, // #FAFAFA
            surfaceVariant = PaletteDark.Gray200, // #262626
            onSurfaceVariant = PaletteDark.Gray500, // #8A8A8A
            outline = PaletteDark.Gray300, // #373737
            error = Color(0xFFCF6679),
            onError = PaletteDark.Base
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
