package com.minimize.uniswap.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Figma Design System Monochrome Tokens (AAA Pass)
// ==========================================

object PaletteLight {
    val Base = Color(0xFFFFFFFF)
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFE5E5E5)
    val Gray300 = Color(0xFFD4D4D4)
    val Gray400 = Color(0xFFA3A3A3)
    val Gray500 = Color(0xFF737373)
    val Gray600 = Color(0xFF525252)
    val Gray700 = Color(0xFF404040)
    val Gray800 = Color(0xFF262626)
    val Gray900 = Color(0xFF171717)
    val Gray950 = Color(0xFF0A0A0A)
}

object PaletteDark {
    val Base = Color(0xFF000000)
    val Gray50 = Color(0xFF0A0A0A)
    val Gray100 = Color(0xFF171717)
    val Gray200 = Color(0xFF262626)
    val Gray300 = Color(0xFF373737)
    val Gray400 = Color(0xFF525252)
    val Gray500 = Color(0xFF8A8A8A)
    val Gray600 = Color(0xFFA3A3A3)
    val Gray700 = Color(0xFFD4D4D4)
    val Gray800 = Color(0xFFE5E5E5)
    val Gray900 = Color(0xFFF5F5F5)
    val Gray950 = Color(0xFFFAFAFA)
}

// Brand & Semantic Accents
val UniSwapEmerald = Color(0xFF146345)
val WasteMetricGreen = Color(0xFF0F8A5F)
val CampusAmber = Color(0xFFFFB300)
val SuccessGreen = Color(0xFF43A047)
val ErrorRed = Color(0xFFB3261E)
val ActionLinkBlue = Color(0xFF3271D7)
val VerifiedStudentGreen = Color(0xFF02B014)

// Surface & Card Tokens
val CardDarkSurface = Color(0xFF121416)
val CardLightContainer = Color(0xFFEBEBEB)
val ContainerHeroDark = Color(0xFF191919)
val ContainerTrendingDark = Color(0xFF1C1D21)

// Buttons & Actions
val BtnChatBg = Color(0xFFD9D9D9)
val BtnChatWithSeller = Color(0xFF22252A)
val BtnMakeOffer = Color(0xFF59626F)
val BtnBackBg = Color(0xFF30353B)

// Text & Placeholder Tokens
val TextSubtle = Color(0xFF959595)
val TextMutedLight = Color(0xFFC7CCD1)
val TextPriceDisplay = Color(0xFFE8E8E8)
val SearchPlaceholderColor = Color(0xFF8A8A8A)
val AuthFieldPlaceholderColor = Color(0xFFBBBBBB)

// Glassmorphism & Navigation
val GlassNavStart = Color(0xFF303030)
val GlassNavEnd = Color(0xFF141518)
val NavIndicatorBg = Color(0xFFD9D9D9)
val PagerDotActive = Color(0xFF767676)
val PagerDotInactive = Color(0xFF303030)

// Legacy alias mappings for backwards-compatibility
val BoneWhite = PaletteLight.Gray50
val DeepTeal = Color(0xFF264653)
val Onyx = PaletteDark.Gray100
val Slate = PaletteDark.Gray200
val LightGrey = PaletteLight.Gray100
val DefaultAccent = UniSwapEmerald

