package com.minimize.uniswap.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.minimize.uniswap.data.model.CampusItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Selling", "Given Away", "Saved")

    val screenBackground = Color(0xFFFBFBF9)

    Scaffold(
        containerColor = screenBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(2.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.08f)),
                    shape = CircleShape,
                    color = Color.White,
                    onClick = onBackClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(2.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.08f)),
                    shape = CircleShape,
                    color = Color.White,
                    onClick = onSettingsClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.fetchProfileData() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. User Avatar & Details
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(108.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=400",
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            // Edit Icon Badge
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .shadow(4.dp, CircleShape),
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = Color(0xFF0F8A5F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Alex Johnson",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF111827)
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "@alexj_eco • Junior",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF8E8E93),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // 2. Sustainability Impact Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE5F9EE),
                                        Color(0xFFF3FCF7)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            // Card Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = Color(0xFF0F8A5F),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sustainability Impact",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = Color(0xFF111827)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // First Row: Impact Score & Waste Diverted
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "450",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = Color(0xFF0F8A5F)
                                        )
                                    )
                                    Text(
                                        text = "Impact Score",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFF859A90)
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "12 lbs",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = Color(0xFF111827)
                                        )
                                    )
                                    Text(
                                        text = "Waste Diverted",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFF859A90)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Second Row: Items Recycled
                            Column {
                                Text(
                                    text = "4",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = Color(0xFF111827)
                                    )
                                )
                                Text(
                                    text = "Items Recycled",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF859A90)
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Segmented Pill Tab Bar
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFEAEAEA),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTabIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .then(
                                            if (isSelected) Modifier.shadow(1.dp, RoundedCornerShape(20.dp))
                                            else Modifier
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedTabIndex = index
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF111827) else Color(0xFF8E8E93)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Dynamic Items List
                val displayItems = when (selectedTabIndex) {
                    0 -> state.sellingItems
                    1 -> state.givenAwayItems
                    else -> state.savedItems
                }

                if (displayItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.error ?: "No items found here.",
                                color = Color(0xFF8E8E93),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(displayItems, key = { it.id }) { item ->
                        ProfileItemCard(
                            item = item,
                            metaIcon = if (selectedTabIndex == 2) Icons.Outlined.FavoriteBorder else Icons.Outlined.Visibility,
                            metaText = if (selectedTabIndex == 2) "3 saves" else "12 views"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItemCard(
    item: CampusItem,
    metaIcon: ImageVector = Icons.Outlined.Visibility,
    metaText: String = "12 views"
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Thumbnail
            AsyncImage(
                model = item.imageUrl.ifBlank { "https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=400" },
                contentDescription = item.title,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title.ifBlank { "TI-84 Graphing Calc" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF111827)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (item.price == 0.0) "FREE" else "$${item.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F8A5F)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = metaIcon,
                        contentDescription = null,
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = metaText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8E8E93)
                        )
                    )
                }
            }

            // More Options Icon
            IconButton(
                onClick = { /* Handle item actions */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = Color(0xFF8E8E93)
                )
            }
        }
    }
}