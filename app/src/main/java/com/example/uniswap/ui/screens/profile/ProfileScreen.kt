package com.example.uniswap.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniswap.ui.components.ActivityCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    // Collecting the reactive UI state from the ViewModel (Network driven)
    val state by viewModel.uiState.collectAsState()

    // Local state for tab selection
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Selling", "Given Away", "Saved")

    // PullToRefresh allows Navneet to see updated stats from the DB instantly
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.fetchProfileData() },
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. User Header (Personalized for Navneet)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N", // Hardcoded for Navneet or use state.userName.take(1)
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Navneet Sindhu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Elite Recycler • Hisar Center",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Sustainability Score Card (Driven by Backend Data)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF14967F), Color(0xFF0D6B5B))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text("YOUR IMPACT", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Sustainability\nScore",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 28.sp,
                            lineHeight = 34.sp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ImpactMetric("Lbs of waste saved", String.format("%.1f", state.lbsSaved))
                        ImpactMetric("Items recycled", state.itemsRecycled.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Collections Tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    tabs.forEachIndexed { index, title ->
                        TabItem(
                            label = title,
                            isSelected = selectedTabIndex == index,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTabIndex = index }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Dynamic List based on current selection
            val displayItems = when (selectedTabIndex) {
                0 -> state.sellingItems
                1 -> state.givenAwayItems
                else -> state.savedItems
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(displayItems, key = { it.id }) { item ->
                    ActivityCard(item = item)
                }

                if (displayItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (state.error != null) state.error!! else "No items found here.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 5. Progress Card
            BadgeProgressCard()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ImpactMetric(label: String, value: String) {
    Column {
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.width(40.dp).height(4.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
        )
    }
}

@Composable
fun BadgeProgressCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🏆", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Next Badge: Waste Zero", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Complete 2 more giveaways to earn!", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}