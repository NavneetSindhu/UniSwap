package com.minimize.uniswap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    // Used for the bottom sheet overlay (56 radius)
    extraLarge = RoundedCornerShape(topStart = 56.dp, topEnd = 56.dp),
    // Used for the button (50 radius)
    large = RoundedCornerShape(50.dp)
)