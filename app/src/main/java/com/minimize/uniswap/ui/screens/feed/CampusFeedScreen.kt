package com.minimize.uniswap.ui.screens.feed

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.minimize.uniswap.ui.components.EmptyStateView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.screens.feed.components.DigitalNotesCard
import com.minimize.uniswap.ui.screens.feed.components.FeedFilterPills
import com.minimize.uniswap.ui.screens.home.components.RecentUploadCard
import com.minimize.uniswap.ui.theme.*

import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.minimize.uniswap.ui.components.BlockUserDialog
import com.minimize.uniswap.ui.components.ItemActionBottomSheet
import com.minimize.uniswap.ui.components.ReportBottomSheet

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
    viewModel: FeedViewModel = hiltViewModel()
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val isSubmittingReport by viewModel.isSubmittingReport.collectAsStateWithLifecycle()
    val isBlockingSeller by viewModel.isBlockingSeller.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val savedItemIds by viewModel.savedItemIds.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var selectedItemForAction by remember { mutableStateOf<CampusItem?>(null) }
    var itemToReport by remember { mutableStateOf<CampusItem?>(null) }
    var itemToBlock by remember { mutableStateOf<CampusItem?>(null) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
                val shareText = context.getString(
                    R.string.action_share_text,
                    targetItem.title,
                    targetItem.price.toInt().toString()
                )
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.action_share_listing)))
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
                // 1. Profile Avatar (42x42) + Capsule Search Bar (50dp height)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile Icon on Left (42x42)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(themeColors.btnBackBg)
                            .clickable(onClick = onProfileClick)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200",
                            contentDescription = stringResource(R.string.field_title_label),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Capsule Search Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(CircleShape)
                            .background(themeColors.cardSurface)
                            .padding(start = 18.dp, end = 10.dp),
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
                                        fontSize = 14.sp,
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
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(themeColors.textPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // 30x30 Action Circle Icon on Right
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.textPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.search_placeholder),
                                    tint = themeColors.background,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                targetState = items,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "feed_grid_transition"
            ) { targetItems ->
                if (targetItems.isEmpty()) {
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
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(targetItems, key = { it.id }) { item ->
                            val isDigitalNote = item.category.name.contains("ENGINEERING", ignoreCase = true) ||
                                    item.title.contains("note", ignoreCase = true) ||
                                    item.title.contains("book", ignoreCase = true) ||
                                    item.title.contains("pdf", ignoreCase = true)

                            if (isDigitalNote) {
                                DigitalNotesCard(
                                    item = item,
                                    isSaved = item.id in savedItemIds,
                                    onSaveClick = { viewModel.toggleSaveItem(item.id) },
                                    onClick = { onItemClick(item) }
                                )
                            } else {
                                RecentUploadCard(
                                    item = item,
                                    isSaved = item.id in savedItemIds,
                                    onSaveClick = { viewModel.toggleSaveItem(item.id) },
                                    onClick = { onItemClick(item) },
                                    onLongClick = { selectedItemForAction = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}