package com.minimize.uniswap.ui.screens.feed

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.components.*
import com.minimize.uniswap.ui.screens.feed.components.CampusScope
import com.minimize.uniswap.ui.screens.feed.components.DigitalNotesCard
import com.minimize.uniswap.ui.screens.feed.components.FeedFilterBottomSheet
import com.minimize.uniswap.ui.screens.feed.components.FeedFilterPills
import com.minimize.uniswap.ui.screens.feed.components.FeedSortOption
import com.minimize.uniswap.ui.screens.home.components.RecentUploadCard
import com.minimize.uniswap.ui.theme.*

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import com.minimize.uniswap.ui.components.nudge.GuestNudgeBottomSheet

/**
 * All Feed Screen ("See All" / Explore Tab).
 * Features standard 50dp capsule search bar matching HomeScreen without brand logo,
 * dynamic category filter pills, and 2-column grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusFeedScreen(
    onItemClick: (CampusItem) -> Unit,
    onProfileClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val isSubmittingReport by viewModel.isSubmittingReport.collectAsStateWithLifecycle()
    val isBlockingSeller by viewModel.isBlockingSeller.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val savedItemIds by viewModel.savedItemIds.collectAsStateWithLifecycle()
    val isGuestMode by viewModel.isGuestMode.collectAsStateWithLifecycle()

    val campusScope by viewModel.campusScope.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val selectedCondition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    val priceRange by viewModel.priceRange.collectAsStateWithLifecycle()
    val freeOnly by viewModel.freeOnly.collectAsStateWithLifecycle()
    val verifiedOnly by viewModel.verifiedOnly.collectAsStateWithLifecycle()
    val activeFilterCount by viewModel.activeFilterCount.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var selectedItemForAction by remember { mutableStateOf<CampusItem?>(null) }
    var itemToReport by remember { mutableStateOf<CampusItem?>(null) }
    var itemToBlock by remember { mutableStateOf<CampusItem?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var isGuestNudgeOpen by remember { mutableStateOf(false) }
    var guestNudgeSubtitle by remember { mutableStateOf("") }
    val toastHostState = com.minimize.uniswap.ui.components.LocalToastHostState.current

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            if (msg.contains("failed", ignoreCase = true) || msg.contains("error", ignoreCase = true)) {
                toastHostState.showError(msg)
            } else {
                toastHostState.showSuccess(msg)
            }
            viewModel.clearUserMessage()
        }
    }

    val themeColors = UniSwapTheme.colors

    // Safety / Action Bottom Sheet triggered on long-press
    selectedItemForAction?.let { targetItem ->
        ItemActionBottomSheet(
            onDismissRequest = { selectedItemForAction = null },
            itemTitle = targetItem.title,
            sellerName = targetItem.sellerName,
            isSellerSelf = targetItem.sellerId == viewModel.currentUserId,
            onShareClick = {
                com.minimize.uniswap.util.ShareUtils.shareProduct(context, targetItem)
                selectedItemForAction = null
            },
            onReportClick = {
                val item = targetItem
                selectedItemForAction = null
                itemToReport = item
            },
            onBlockClick = {
                val item = targetItem
                selectedItemForAction = null
                itemToBlock = item
            }
        )
    }

    itemToReport?.let { reportItem ->
        ReportBottomSheet(
            onDismissRequest = { itemToReport = null },
            isSubmitting = isSubmittingReport,
            onSubmitReport = { reason, details ->
                viewModel.submitReport(reportItem, reason, details) { success ->
                    if (success) {
                        itemToReport = null
                    }
                }
            }
        )
    }

    itemToBlock?.let { blockItem ->
        BlockUserDialog(
            userName = blockItem.sellerName,
            isBlocking = isBlockingSeller,
            onConfirmBlock = {
                viewModel.blockSeller(blockItem.sellerId) {
                    itemToBlock = null
                }
            },
            onDismiss = { itemToBlock = null }
        )
    }

    // Filter & Sort Bottom Sheet
    if (showFilterSheet) {
        FeedFilterBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            campusScope = campusScope,
            onCampusScopeChanged = { viewModel.setCampusScope(it) },
            campusName = userProfile?.campusCenter,
            selectedSort = selectedSort,
            onSortSelected = { viewModel.setSortOption(it) },
            selectedCondition = selectedCondition,
            onConditionSelected = { viewModel.setCondition(it) },
            priceRange = priceRange,
            onPriceRangeChanged = { viewModel.setPriceRange(it) },
            freeOnly = freeOnly,
            onFreeOnlyChanged = { viewModel.setFreeOnly(it) },
            verifiedOnly = verifiedOnly,
            onVerifiedOnlyChanged = { viewModel.setVerifiedOnly(it) },
            onResetAll = { viewModel.resetAllFilters() },
            activeFilterCount = activeFilterCount
        )
    }

    Scaffold(
        containerColor = themeColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // 1. Profile Avatar (42x42) + Capsule Search Bar (50dp height) + Filter Button (42x42)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Icon on Left (42x42)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(themeColors.btnBackBg)
                            .clickable(onClick = onProfileClick)
                    ) {
                        UserAvatar(
                            avatarId = userProfile?.avatarId,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Capsule Search Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(themeColors.cardSurface)
                            .padding(start = 16.dp, end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_placeholder),
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = themeColors.textSubtle
                                    )
                                }

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.textPrimary,
                                        fontSize = 13.sp
                                    ),
                                    cursorBrush = SolidColor(themeColors.textPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // 28x28 Search Icon
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.textPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.search_placeholder),
                                    tint = themeColors.background,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Filter Action Button with Unclipped Badge
                    Box(
                        modifier = Modifier.size(42.dp)
                    ) {
                        // 42x42 Circular Button Base
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (activeFilterCount > 0) themeColors.textPrimary else themeColors.cardSurface)
                                .clickable { showFilterSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = stringResource(R.string.filter_sheet_title),
                                tint = if (activeFilterCount > 0) themeColors.background else themeColors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Active Filter Counter Badge
                        if (activeFilterCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                                    .border(1.5.dp, themeColors.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeFilterCount.toString(),
                                    color = Color.White,
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Category Filter Pills
                FeedFilterPills(
                    categories = categories,
                    selectedCategoryId = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchItems() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = when {
                    isLoading -> "LOADING"
                    items.isEmpty() -> "EMPTY"
                    else -> "CONTENT"
                },
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
                        .togetherWith(fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)))
                },
                label = "feed_grid_transition"
            ) { targetState ->
                when (targetState) {
                    "LOADING" -> {
                        AppSkeletonView(type = SkeletonType.FEED_GRID)
                    }
                    "EMPTY" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (searchQuery.isNotBlank()) {
                                EmptyStateView(
                                    title = stringResource(R.string.empty_search_title),
                                    subtitle = stringResource(R.string.empty_search_subtitle),
                                    lottieRes = R.raw.anim_user_search,
                                    fallbackIcon = Icons.Outlined.SearchOff,
                                    ctaText = stringResource(R.string.empty_search_cta),
                                    onCtaClick = { viewModel.updateSearchQuery("") }
                                )
                            } else {
                                EmptyStateView(
                                    title = stringResource(R.string.empty_category_title),
                                    subtitle = stringResource(R.string.empty_category_subtitle),
                                    lottieRes = R.raw.anim_empty_feed,
                                    fallbackIcon = Icons.Outlined.Inventory2
                                )
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                val isDigitalNote = item.category.name.contains("ENGINEERING", ignoreCase = true) ||
                                        item.title.contains("note", ignoreCase = true) ||
                                        item.title.contains("book", ignoreCase = true) ||
                                        item.title.contains("pdf", ignoreCase = true)

                                if (isDigitalNote) {
                                    DigitalNotesCard(
                                        item = item,
                                        isSaved = item.id in savedItemIds,
                                        onSaveClick = {
                                            if (isGuestMode) {
                                                guestNudgeSubtitle = context.getString(R.string.guest_nudge_favorite_subtitle)
                                                isGuestNudgeOpen = true
                                            } else {
                                                viewModel.toggleSaveItem(item.id)
                                            }
                                        },
                                        onClick = { onItemClick(item) },
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = tween(220),
                                            fadeOutSpec = tween(160),
                                            placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                    )
                                } else {
                                    RecentUploadCard(
                                        item = item,
                                        isSaved = item.id in savedItemIds,
                                        onSaveClick = {
                                            if (isGuestMode) {
                                                guestNudgeSubtitle = context.getString(R.string.guest_nudge_favorite_subtitle)
                                                isGuestNudgeOpen = true
                                            } else {
                                                viewModel.toggleSaveItem(item.id)
                                            }
                                        },
                                        onClick = { onItemClick(item) },
                                        onLongClick = { selectedItemForAction = item },
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = tween(220),
                                            fadeOutSpec = tween(160),
                                            placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
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

    if (isGuestNudgeOpen) {
        GuestNudgeBottomSheet(
            onDismissRequest = { isGuestNudgeOpen = false },
            onSignInClick = {
                isGuestNudgeOpen = false
                onSignInClick()
            },
            onSignUpClick = {
                isGuestNudgeOpen = false
                onSignUpClick()
            },
            subtitle = guestNudgeSubtitle
        )
    }
}
