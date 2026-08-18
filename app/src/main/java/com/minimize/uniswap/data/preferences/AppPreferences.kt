package com.minimize.uniswap.data.preferences

/**
 * Supported Theme Modes.
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/**
 * Supported Typography Styles.
 */
enum class TypographyStyle {
    MODERN, EDITORIAL
}

/**
 * User Preference State.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val accentColorHex: String = "#146345", // Default UniSwap Emerald
    val typographyStyle: TypographyStyle = TypographyStyle.MODERN,
    val campusCenter: String = "Main Campus",
    val isVerified: Boolean = false,
    val pushNotificationsEnabled: Boolean = true,
    val emailDigestEnabled: Boolean = false
)
