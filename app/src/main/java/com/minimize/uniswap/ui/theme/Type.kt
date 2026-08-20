package com.minimize.uniswap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font // Added for local fonts (R.font...)
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GFont // Aliased to avoid clash
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.data.preferences.TypographyStyle

// Google Fonts Provider Setup
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Use the aliased GFont for downloadable fonts
private val Inter = FontFamily(GFont(googleFont = GoogleFont("Inter"), fontProvider = provider))
private val Playfair = FontFamily(GFont(googleFont = GoogleFont("Playfair Display"), fontProvider = provider))
private val Roboto = FontFamily(GFont(googleFont = GoogleFont("Roboto"), fontProvider = provider))

fun getTypography(style: TypographyStyle): Typography {
    val headlineFont = if (style == TypographyStyle.EDITORIAL) Playfair else Inter
    val bodyFont = if (style == TypographyStyle.EDITORIAL) Inter else Roboto

    return Typography(
        displayLarge = TextStyle(
            fontFamily = headlineFont,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headlineFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = headlineFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    )
}

// Define the Matter font family using the standard local Font import
val MatterFontFamily = FontFamily(
    Font(R.font.matter_medium, FontWeight.Medium),
    Font(R.font.matter_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    // Mapping the typography spec from image_ffa512.png
    displayMedium = TextStyle(
        fontFamily = MatterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 33.6.sp, // 105% of 32
        letterSpacing = (-0.64).sp, // -2% of 32
        color = Color.White
    )
)