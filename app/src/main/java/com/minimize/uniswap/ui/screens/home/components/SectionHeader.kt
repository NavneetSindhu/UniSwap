package com.minimize.uniswap.ui.screens.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SectionHeader(
    title: String,
    actionContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeSectionHeader(
        title = title,
        actionContent = actionContent,
        modifier = modifier
    )
}