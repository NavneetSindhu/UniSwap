package com.minimize.uniswap.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Reusable Section Header for Home Screen.
 * Uses Matter Medium 16sp title and theme-aware text colors.
 */
@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionContent: @Composable () -> Unit = {}
) {
    val themeColors = UniSwapTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = themeColors.textPrimary
        )
        actionContent()
    }
}

