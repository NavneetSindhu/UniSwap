package com.example.uniswap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniswap.data.model.CampusItem

@Composable
fun ActivityCard(item: CampusItem) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for Item Image
            Box(
                modifier = Modifier.size(60.dp).background(Color.LightGray, RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = if (item.isFree) "Given Away" else "Sold for $${item.price}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            // Simple status indicator
            Box(
                modifier = Modifier.size(8.dp).background(Color(0xFF8AB17D), RoundedCornerShape(4.dp))
            )
        }
    }
}