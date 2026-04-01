package com.example.uniswap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit // Added this parameter
) {
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(24.dp)) // Ensures the click ripple is rounded
            .clickable { onClick() }, // Handles the tap
        shape = RoundedCornerShape(24.dp),
        // Use primary color if selected, otherwise a subtle muted background
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        },
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                // White text on orange (primary), dark teal on muted background
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}