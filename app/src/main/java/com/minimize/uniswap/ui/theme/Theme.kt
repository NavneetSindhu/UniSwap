package com.minimize.uniswap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.toColorInt
import com.minimize.uniswap.data.preferences.ThemeMode
import com.minimize.uniswap.data.preferences.TypographyStyle

@Composable
fun UniSwapTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    typographyStyle: TypographyStyle = TypographyStyle.MODERN,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> UniSwapColorPalette.darkScheme()
        else -> UniSwapColorPalette.lightScheme()
    }

    val extendedColors = ExtendedColors(
        isDark = darkTheme,
        background = if (darkTheme) PaletteDark.Base else PaletteLight.Base,
        cardSurface = if (darkTheme) CardDarkSurface else PaletteLight.Gray100,
        cardSurfaceVariant = if (darkTheme) PaletteDark.Gray200 else PaletteLight.Gray200,
        cardBackground = if (darkTheme) CardDarkSurface else PaletteLight.Gray100,
        divider = if (darkTheme) PaletteDark.Gray300 else PaletteLight.Gray200,
        inputBackground = if (darkTheme) PaletteDark.Gray100 else PaletteLight.Gray100,
        textPrimary = if (darkTheme) Color.White else PaletteLight.Gray950,
        textSecondary = if (darkTheme) TextMutedLight else PaletteLight.Gray600,
        textSubtle = if (darkTheme) TextSubtle else PaletteLight.Gray500,
        btnBackBg = if (darkTheme) BtnBackBg else PaletteLight.Gray200,
        glassNavStart = if (darkTheme) GlassNavStart else Color(0xFFE5E7EB),
        glassNavEnd = if (darkTheme) GlassNavEnd else Color(0xFFD1D5DB),
        navIndicatorBg = if (darkTheme) NavIndicatorBg else PaletteLight.Gray950,
        navIndicatorIconTint = if (darkTheme) PaletteLight.Gray950 else Color.White,
        wasteMetricGreen = WasteMetricGreen,
        campusAmber = CampusAmber,
        success = SuccessGreen,
        surfaceHighlight = if (darkTheme) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    )

    val typography = AppTypography
    val dimens = UniSwapDimens()

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalUniSwapDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

/**
 * Accessor for custom design tokens.
 */
object UniSwapTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val dimens: UniSwapDimens
        @Composable
        get() = LocalUniSwapDimens.current
}
