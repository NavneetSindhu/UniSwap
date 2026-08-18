package com.minimize.uniswap.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.components.AppSearchBar
import com.minimize.uniswap.ui.components.TrendingItemCard

private val DarkBackground = Color(0xFF121316)
private val CardDarkSurface = Color(0xFF1C1D21)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusFeedScreen(
    onItemClick: (CampusItem) -> Unit,
    onProfileClick: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchItems() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Top Header (Avatar + Search Bar)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200",
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onProfileClick),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        AppSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Trending Section
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trending on Campus",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 17.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.NorthEast,
                                contentDescription = "See All Trending",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(items, key = { "trending_${it.id}" }) { item ->
                                TrendingItemCard(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onConnectClick = { onItemClick(item) }
                                )
                            }
                        }
                    }
                }

                // 3. Near You Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Near You",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF4C8DFF),
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.clickable { /* Handle See all */ }
                        )
                    }
                }

                // 4. Asymmetric Near You Grid Layout
                item {
                    val firstItem = items.firstOrNull()
                    val remainingItems = items.drop(1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Column: Large Product Card
                        if (firstItem != null) {
                            Surface(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { onItemClick(firstItem) },
                                color = CardDarkSurface
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = firstItem.imageUrl.ifBlank { "https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=400" },
                                            contentDescription = firstItem.title,
                                            modifier = Modifier.fillMaxSize().padding(12.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = firstItem.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Navneet Sindhu",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = "Dept. of Robotics",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.Gray,
                                                        fontSize = 8.sp
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            text = "$${firstItem.price.toInt()}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { onItemClick(firstItem) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFE2E8F0),
                                            contentColor = Color(0xFF181A20)
                                        )
                                    ) {
                                        Text("Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // Right Column: Stacked Gradient Cards
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            remainingItems.take(2).forEachIndexed { index, item ->
                                val gradientColors = if (index % 2 == 0) {
                                    listOf(Color(0xFF0F3B2E), Color(0xFF78972E))
                                } else {
                                    listOf(Color(0xFF14244E), Color(0xFF2A5BB8))
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Brush.linearGradient(gradientColors))
                                        .clickable { onItemClick(item) }
                                        .padding(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    lineHeight = 13.sp
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "12 PDFs",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 9.sp
                                                )
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                onClick = { onItemClick(item) },
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color.White.copy(alpha = 0.9f)
                                            ) {
                                                Text(
                                                    text = "Chat",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF181A20)
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                                )
                                            }

                                            Text(
                                                text = "$${item.price.toInt()}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (index % 2 == 0) Color(0xFFCBE86B) else Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}