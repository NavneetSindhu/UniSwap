package com.minimize.uniswap.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.components.AppSkeletonView
import com.minimize.uniswap.ui.components.SkeletonType
import com.minimize.uniswap.ui.screens.feed.FeedViewModel
import com.minimize.uniswap.ui.screens.home.components.HomeSectionHeader
import com.minimize.uniswap.ui.screens.home.components.HomeTopHeader
import com.minimize.uniswap.ui.screens.home.components.RecentUploadCard
import com.minimize.uniswap.ui.screens.home.components.TrendingCarousel
import androidx.compose.material.icons.outlined.Inventory2
import com.minimize.uniswap.ui.components.EmptyStateView
import com.minimize.uniswap.ui.theme.ActionLinkBlue
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.PaletteDark
import com.minimize.uniswap.ui.theme.UniSwapTheme

import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.minimize.uniswap.ui.components.BlockUserDialog
import com.minimize.uniswap.ui.components.ItemActionBottomSheet
import com.minimize.uniswap.ui.components.ReportBottomSheet
import com.minimize.uniswap.ui.components.nudge.GuestNudgeBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (CampusItem) -> Unit,
    onProfileClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isSubmittingReport by viewModel.isSubmittingReport.collectAsStateWithLifecycle()
    val isBlockingSeller by viewModel.isBlockingSeller.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val savedItemIds by viewModel.savedItemIds.collectAsStateWithLifecycle()
    val isGuestMode by viewModel.isGuestMode.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var selectedItemForAction by remember { mutableStateOf<CampusItem?>(null) }
    var itemToReport by remember { mutableStateOf<CampusItem?>(null) }
    var itemToBlock by remember { mutableStateOf<CampusItem?>(null) }
    var isGuestNudgeOpen by remember { mutableStateOf(false) }
    var guestNudgeSubtitle by remember { mutableStateOf("") }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

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

    Scaffold(
        containerColor = UniSwapTheme.colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            HomeTopHeader(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                avatarId = userProfile?.avatarId,
                onProfileClick = onProfileClick
            )
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
                    (fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.98f, animationSpec = tween(240, easing = FastOutSlowInEasing)))
                        .togetherWith(fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)))
                },
                label = "home_screen_feed_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetState ->
                when (targetState) {
                    "LOADING" -> {
                        AppSkeletonView(type = SkeletonType.HOME_FEED)
                    }
                    "EMPTY" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateView(
                                title = stringResource(R.string.empty_feed_title),
                                subtitle = stringResource(R.string.empty_feed_subtitle),
                                fallbackIcon = Icons.Outlined.Inventory2
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(28.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
                        ) {
                            // 1. Section 1: "Trending on Campus" Carousel (Endless Auto-scroll + Left/Right Peek Cards)
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    HomeSectionHeader(
                                        title = stringResource(R.string.trending_on_campus),
                                        actionContent = {
                                            Icon(
                                                imageVector = Icons.Default.NorthEast,
                                                contentDescription = stringResource(R.string.trending_on_campus),
                                                tint = UniSwapTheme.colors.textPrimary,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable(onClick = onSeeAllClick)
                                            )
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TrendingCarousel(
                                        items = items,
                                        savedItemIds = savedItemIds,
                                        onSaveClick = { item ->
                                            if (isGuestMode) {
                                                guestNudgeSubtitle = context.getString(R.string.guest_nudge_favorite_subtitle)
                                                isGuestNudgeOpen = true
                                            } else {
                                                viewModel.toggleSaveItem(item.id)
                                            }
                                        },
                                        onItemClick = onItemClick
                                    )
                                }
                            }

                            // 2. Section 2: "Recently Uploads" Section
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    HomeSectionHeader(
                                        title = stringResource(R.string.recently_uploads),
                                        actionContent = {
                                            Text(
                                                text = stringResource(R.string.see_all),
                                                fontFamily = MatterFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = ActionLinkBlue,
                                                modifier = Modifier.clickable(onClick = onSeeAllClick)
                                            )
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 24.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        items(items, key = { "recent_${it.id}" }) { item ->
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