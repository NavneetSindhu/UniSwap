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
import com.minimize.uniswap.ui.theme.ActionLinkBlue
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.PaletteDark
import com.minimize.uniswap.ui.theme.UniSwapTheme

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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp), // Clear floating bottom nav
                verticalArrangement = Arrangement.spacedBy(16.dp)
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

                        if (items.isNotEmpty()) {
                            TrendingCarousel(
                                items = items,
                                onItemClick = onItemClick
                            )
                        }
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
                                    onClick = { onItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}