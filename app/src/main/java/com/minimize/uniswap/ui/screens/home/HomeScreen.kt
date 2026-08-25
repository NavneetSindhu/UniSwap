package com.minimize.uniswap.ui.screens.home

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (CampusItem) -> Unit,
    onProfileClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isSubmittingReport by viewModel.isSubmittingReport.collectAsStateWithLifecycle()
    val isBlockingSeller by viewModel.isBlockingSeller.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var selectedItemForAction by remember { mutableStateOf<CampusItem?>(null) }
    var isReportSheetOpen by remember { mutableStateOf(false) }
    var isBlockDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    // Safety / Action Bottom Sheet triggered on long-press
    if (selectedItemForAction != null && !isReportSheetOpen && !isBlockDialogOpen) {
        val targetItem = selectedItemForAction!!
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
            onReportClick = { isReportSheetOpen = true },
            onBlockClick = { isBlockDialogOpen = true }
        )
    }

    if (isReportSheetOpen && selectedItemForAction != null) {
        ReportBottomSheet(
            onDismissRequest = {
                isReportSheetOpen = false
                selectedItemForAction = null
            },
            isSubmitting = isSubmittingReport,
            onSubmitReport = { reason, details ->
                viewModel.submitReport(selectedItemForAction!!, reason, details) { success ->
                    if (success) {
                        isReportSheetOpen = false
                        selectedItemForAction = null
                    }
                }
            }
        )
    }

    if (isBlockDialogOpen && selectedItemForAction != null) {
        BlockUserDialog(
            userName = selectedItemForAction!!.sellerName,
            isBlocking = isBlockingSeller,
            onConfirmBlock = {
                viewModel.blockSeller(selectedItemForAction!!.sellerId) {
                    isBlockDialogOpen = false
                    selectedItemForAction = null
                }
            },
            onDismiss = {
                isBlockDialogOpen = false
                selectedItemForAction = null
            }
        )
    }

    Scaffold(
        containerColor = UniSwapTheme.colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            HomeTopHeader(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                profilePicUrl = userProfile?.profilePicUrl?.ifBlank { null },
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
            if (items.isEmpty()) {
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
            } else {
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
}