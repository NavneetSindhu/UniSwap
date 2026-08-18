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
    dynamicColor: Boolean = true,
    accentColorHex: String = "#146345",
    typographyStyle: TypographyStyle = TypographyStyle.MODERN,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val accentColor = Color(accentColorHex.toColorInt())

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> UniSwapColorPalette.darkScheme(accentColor)
        else -> UniSwapColorPalette.lightScheme(accentColor)
    }

    val extendedColors = ExtendedColors(
        wasteMetricGreen = WasteMetricGreen,
        campusAmber = CampusAmber,
        success = SuccessGreen,
        surfaceHighlight = if (darkTheme) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    )

    val typography = getTypography(typographyStyle)
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
