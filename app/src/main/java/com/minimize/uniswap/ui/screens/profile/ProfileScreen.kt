package com.minimize.uniswap.ui.screens.profile

import android.annotation.SuppressLint
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
import com.minimize.uniswap.ui.components.EmptyStateView
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
import androidx.compose.ui.res.stringResource
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
import com.minimize.uniswap.ui.components.UserAvatar
import com.minimize.uniswap.ui.screens.profile.components.InlineAvatarSelector
import com.minimize.uniswap.ui.theme.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onItemClick: (CampusItem) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isEditingAvatar by remember { mutableStateOf(false) }
    val tabs = listOf(
        stringResource(R.string.profile_tab_selling),
        stringResource(R.string.profile_tab_given_away),
        stringResource(R.string.profile_tab_saved)
    )

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
                        contentDescription = stringResource(R.string.action_back),
                        tint = colors.textPrimary,
                        modifier = Modifier
                            .width(18.dp)
                            .height(10.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.profile_title),
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
                        contentDescription = stringResource(R.string.settings_title),
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
                contentPadding = PaddingValues(start = dimens.spaceLarge, end = dimens.spaceLarge, top = dimens.spaceSmall, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceLarge)
            ) {
                // 1. User Avatar & Details
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InlineAvatarSelector(
                            currentAvatarId = state.avatarId,
                            isEditing = isEditingAvatar,
                            onEditChange = { isEditingAvatar = it },
                            onAvatarSaved = { newAvatarId ->
                                viewModel.updateAvatar(newAvatarId)
                            }
                        )

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

                        val context = LocalContext.current
                        val userHandle = if (state.userEmail.isNotBlank()) "@${state.userEmail.substringBefore("@")}" else "@student"
                        val campusAbbr = com.minimize.uniswap.util.CampusMapper.toAbbreviation(context, state.campusCenter)
                        val handleText = if (campusAbbr.isNotBlank()) "$userHandle • $campusAbbr" else userHandle
                        Text(
                            text = handleText,
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Academic & Impact Info Tag Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.gradYear.isNotBlank()) {
                                ProfileTagChip(
                                    icon = Icons.Outlined.School,
                                    label = stringResource(R.string.profile_class_of, state.gradYear),
                                    textColor = colors.textPrimary,
                                    bgColor = colors.cardSurface
                                )
                            }

                            ProfileTagChip(
                                icon = if (state.isVerified) Icons.Outlined.Verified else Icons.Outlined.Person,
                                label = if (state.isVerified) stringResource(R.string.verified_student) else stringResource(R.string.profile_campus_member),
                                textColor = if (state.isVerified) colors.wasteMetricGreen else colors.textSecondary,
                                bgColor = colors.cardSurface
                            )

                            ProfileTagChip(
                                icon = Icons.Outlined.Recycling,
                                label = stringResource(R.string.profile_items_recycled_tag, state.itemsRecycled),
                                textColor = colors.textPrimary,
                                bgColor = colors.cardSurface
                            )
                        }
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
                            .padding(14.dp)
                    ) {
                        Column {
                            // Header Row: Leaf Icon + Title + Eco Tier Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        text = stringResource(R.string.profile_sustainability_impact),
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                }

                                // Eco Tier Badge
                                val tierText = when (state.ecoTier) {
                                    "champion" -> stringResource(R.string.profile_eco_tier_champion)
                                    "contributor" -> stringResource(R.string.profile_eco_tier_contributor)
                                    else -> stringResource(R.string.profile_eco_tier_starter)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.wasteMetricGreen.copy(alpha = 0.18f))
                                        .border(0.5.dp, colors.wasteMetricGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tierText,
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = colors.wasteMetricGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3-Metric Circular Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Metric 1: Items Rehomed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.cardSurface.copy(alpha = 0.7f))
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = state.itemsRecycled.toString(),
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = colors.wasteMetricGreen
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.profile_metric_rehomed),
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center,
                                            color = colors.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Metric 2: Waste Diverted (kg)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.cardSurface.copy(alpha = 0.7f))
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = stringResource(R.string.profile_metric_kg_unit, state.kgSaved),
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.profile_metric_waste_diverted),
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center,
                                            color = colors.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Metric 3: CO2 Offset (kg)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.cardSurface.copy(alpha = 0.7f))
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = stringResource(R.string.profile_metric_kg_unit, state.co2Saved),
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.profile_metric_co2_offset),
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center,
                                            color = colors.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sustainability Motto Subtitle
                            Text(
                                text = stringResource(R.string.profile_sustainability_motto),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = colors.textSecondary.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
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
                            val emptyTitle = when (tabIndex) {
                                0 -> stringResource(R.string.empty_my_listings_title)
                                1 -> stringResource(R.string.empty_sold_title)
                                else -> stringResource(R.string.empty_saved_title)
                            }
                            val emptySubtitle = when (tabIndex) {
                                0 -> stringResource(R.string.empty_my_listings_subtitle)
                                1 -> stringResource(R.string.empty_sold_subtitle)
                                else -> stringResource(R.string.empty_saved_subtitle)
                            }
                            val fallbackIcon = when (tabIndex) {
                                0 -> Icons.Outlined.Storefront
                                1 -> Icons.Outlined.CheckCircleOutline
                                else -> Icons.Outlined.FavoriteBorder
                            }
                            val lottieAnim = R.raw.anim_cat_relaxing

                            EmptyStateView(
                                title = emptyTitle,
                                subtitle = emptySubtitle,
                                lottieRes = lottieAnim,
                                fallbackIcon = fallbackIcon,
                                animationSize = 135.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            val metaSaved = stringResource(R.string.profile_meta_saved)
                            val metaGiven = stringResource(R.string.profile_meta_given_away)
                            val metaAvailable = stringResource(R.string.profile_meta_available)

                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                displayItems.forEach { item ->
                                    ProfileItemCard(
                                        item = item,
                                        metaIcon = if (tabIndex == 2) Icons.Outlined.FavoriteBorder else Icons.Outlined.Visibility,
                                        metaText = if (tabIndex == 2) metaSaved else if (item.status == ItemStatus.SOLD) metaGiven else metaAvailable,
                                        isSavedTab = tabIndex == 2,
                                        onClick = { onItemClick(item) },
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
private fun ProfileTagChip(
    icon: ImageVector,
    label: String,
    textColor: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun ProfileItemCard(
    item: CampusItem,
    metaIcon: ImageVector = Icons.Outlined.Visibility,
    metaText: String = stringResource(R.string.profile_meta_available),
    isSavedTab: Boolean = false,
    onClick: () -> Unit = {},
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
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
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
                        contentDescription = stringResource(R.string.action_share_listing),
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
                                    text = if (item.status == ItemStatus.AVAILABLE) stringResource(R.string.action_mark_sold) else stringResource(R.string.action_mark_available),
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
                                text = stringResource(R.string.action_share_listing),
                                fontFamily = MatterFontFamily,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                        },
                        onClick = {
                            showMenu = false
                            com.minimize.uniswap.util.ShareUtils.shareProduct(context, item)
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
                                text = if (isSavedTab) stringResource(R.string.action_remove_saved) else stringResource(R.string.action_delete_listing),
                                fontFamily = MatterFontFamily,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isSavedTab) Icons.Outlined.FavoriteBorder else Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}
