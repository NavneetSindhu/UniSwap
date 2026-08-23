package com.minimize.uniswap.ui.screens.profile

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.ui.theme.*

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

    val dimens = UniSwapTheme.dimens
    val colors = UniSwapTheme.colors

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = dimens.spaceLarge, vertical = dimens.spaceSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier
                            .width(18.dp)
                            .height(10.dp)
                    )
                }

                Text(
                    text = "Profile",
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = colors.textPrimary
                )

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
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
                contentPadding = PaddingValues(horizontal = dimens.spaceLarge, vertical = dimens.spaceSmall),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceLarge)
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
                                model = state.userPhotoUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=400",
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .border(2.dp, colors.cardSurface, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .shadow(4.dp, CircleShape),
                                shape = CircleShape,
                                color = colors.cardSurface
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = colors.wasteMetricGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User Name with Verified Student Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.userName,
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = colors.textPrimary
                            )
                            if (state.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.ic_verified),
                                    contentDescription = "Verified Student",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val userHandle = if (state.userEmail.isNotBlank()) "@${state.userEmail.substringBefore("@")}" else "@student"
                        Text(
                            text = "$userHandle • Student Member • Campus Center",
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // 2. Sustainability Impact Card matching UniSwap design
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        colors.wasteMetricGreen.copy(alpha = 0.16f),
                                        colors.wasteMetricGreen.copy(alpha = 0.04f)
                                    )
                                )
                            )
                            .border(1.dp, colors.wasteMetricGreen.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.wasteMetricGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Eco,
                                        contentDescription = null,
                                        tint = colors.wasteMetricGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sustainability Impact",
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "450",
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = colors.wasteMetricGreen
                                    )
                                    Text(
                                        text = "Impact Score",
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    val lbsText = "%.1f lbs".format(state.lbsSaved)
                                    Text(
                                        text = lbsText,
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Waste Diverted",
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Column {
                                Text(
                                    text = state.itemsRecycled.toString(),
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Items Recycled & Reused",
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // 3. Smooth Segmented Pill Tab Bar
                item {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardSurface)
                            .padding(4.dp)
                    ) {
                        val tabWidth = maxWidth / tabs.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * selectedTabIndex,
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            label = "tab_indicator_offset"
                        )

                        // Sliding Active Pill Indicator
                        Box(
                            modifier = Modifier
                                .offset(x = indicatorOffset)
                                .width(tabWidth)
                                .fillMaxHeight()
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.btnBackBg)
                        )

                        // Tab Titles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTabIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedTabIndex = index
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontFamily = MatterFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.textPrimary else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Animated Tab Content
                item {
                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width / 3 } + fadeIn(animationSpec = tween(250)))
                                    .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(animationSpec = tween(200)))
                            } else {
                                (slideInHorizontally { width -> -width / 3 } + fadeIn(animationSpec = tween(250)))
                                    .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(animationSpec = tween(200)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "profile_tabs_content"
                    ) { tabIndex ->
                        val displayItems = when (tabIndex) {
                            0 -> state.sellingItems
                            1 -> state.givenAwayItems
                            else -> state.savedItems
                        }

                        if (displayItems.isEmpty() && !state.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.error ?: "No items found in this section.",
                                    fontFamily = MatterFontFamily,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                displayItems.forEach { item ->
                                    ProfileItemCard(
                                        item = item,
                                        metaIcon = if (tabIndex == 2) Icons.Outlined.FavoriteBorder else Icons.Outlined.Visibility,
                                        metaText = if (tabIndex == 2) "Saved" else if (item.status == ItemStatus.SOLD) "Given Away" else "Available",
                                        isSavedTab = tabIndex == 2,
                                        onToggleStatus = { viewModel.toggleItemSoldStatus(item) },
                                        onDelete = {
                                            if (tabIndex == 2) viewModel.removeSavedItem(item.id)
                                            else viewModel.deleteItem(item.id)
                                        }
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

@Composable
fun ProfileItemCard(
    item: CampusItem,
    metaIcon: ImageVector = Icons.Outlined.Visibility,
    metaText: String = "Available",
    isSavedTab: Boolean = false,
    onToggleStatus: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val dimens = UniSwapTheme.dimens
    val colors = UniSwapTheme.colors
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl.ifBlank { item.category.getPlaceholderUrl() },
                contentDescription = item.title,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(dimens.spaceMedium))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (item.price == 0.0) "FREE" else "₹${item.price.toInt()}",
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.wasteMetricGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = metaIcon,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = metaText,
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // 3-Dot More Menu with working dropdown options
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = colors.textSecondary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(colors.cardSurface)
                ) {
                    if (!isSavedTab) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (item.status == ItemStatus.AVAILABLE) "Mark as Sold / Given" else "Mark as Available",
                                    fontFamily = MatterFontFamily,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleStatus()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.wasteMetricGreen
                                )
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Share Item",
                                fontFamily = MatterFontFamily,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                        },
                        onClick = {
                            showMenu = false
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out ${item.title} on UniSwap!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Item"))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                tint = colors.textPrimary
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isSavedTab) "Remove from Saved" else "Delete Listing",
                                fontFamily = MatterFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFFFF5252)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = Color(0xFFFF5252)
                            )
                        }
                    )
                }
            }
        }
    }
}
