package com.minimize.uniswap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Supported screen skeleton layouts across the UniSwap application.
 */
enum class SkeletonType {
    HOME_FEED,
    FEED_GRID,
    MESSAGES_INBOX,
    CHAT_THREAD
}

/**
 * Unified skeleton loading view composable for UniSwap screens.
 * Renders pixel-perfect shimmer placeholder layouts matching the app's exact design system.
 */
@Composable
fun AppSkeletonView(
    type: SkeletonType,
    modifier: Modifier = Modifier
) {
    when (type) {
        SkeletonType.HOME_FEED -> HomeFeedSkeleton(modifier = modifier)
        SkeletonType.FEED_GRID -> FeedGridSkeleton(modifier = modifier)
        SkeletonType.MESSAGES_INBOX -> MessagesInboxSkeleton(modifier = modifier)
        SkeletonType.CHAT_THREAD -> ChatThreadSkeleton(modifier = modifier)
    }
}

/**
 * Skeleton layout matching HomeScreen exactly (Trending Carousel 252x200 + Recent Uploads 184x262).
 */
@Composable
private fun HomeFeedSkeleton(modifier: Modifier = Modifier) {
    val colors = UniSwapTheme.colors
    val isDark = colors.isDark

    // Contrast colors so inner elements are distinctly visible on top of the carousel image even without sweep
    val overlayPillBase = if (isDark) Color(0xFF353C47) else Color(0xFFC0C9D3)
    val overlayPillHighlight = if (isDark) Color(0xFF485160) else Color(0xFFDDE3E9)

    val overlayTextBase = if (isDark) Color(0xFF505866) else Color(0xFF8895A5)
    val overlayTextHighlight = if (isDark) Color(0xFF6F7A8D) else Color(0xFFADB9C6)

    val overlayBtnBase = if (isDark) Color(0xFF454D5A) else Color(0xFF9EABB8)
    val overlayBtnHighlight = if (isDark) Color(0xFF5A6475) else Color(0xFFB8C3CE)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        userScrollEnabled = false
    ) {
        // 1. Trending Carousel Section (Matching 210dp height with 252x200 center card)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section Title Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(160.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .size(18.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Carousel Preview Row matching TrendingCarousel proportions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left peek card (206x165)
                        Box(
                            modifier = Modifier
                                .width(65.dp)
                                .height(165.dp)
                                .offset(x = (-8).dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                                .background(colors.cardSurface)
                                .padding(8.dp)
                        ) {
                            ShimmerBox(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Hero center card (252x200, corner radius 24dp)
                        Box(
                            modifier = Modifier
                                .width(252.dp)
                                .height(200.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(colors.cardSurface)
                        ) {
                            // Full-bleed product image shimmer
                            ShimmerBox(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(24.dp)
                            )

                            // Bottom Warm/Dark Gradient Overlay for realistic layered contrast
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.25f),
                                                Color.Black.copy(alpha = 0.65f)
                                            ),
                                            startY = 50f
                                        )
                                    )
                            )

                            // Top-left price badge pill (74x24)
                            ShimmerBox(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 12.dp)
                                    .align(Alignment.TopStart)
                                    .size(width = 74.dp, height = 24.dp),
                                shape = RoundedCornerShape(12.dp),
                                customBaseColor = overlayPillBase,
                                customHighlightColor = overlayPillHighlight
                            )

                            // Top-right save heart circle (24dp)
                            ShimmerCircle(
                                size = 24.dp,
                                modifier = Modifier
                                    .padding(top = 8.dp, end = 8.dp)
                                    .align(Alignment.TopEnd),
                                customBaseColor = overlayPillBase,
                                customHighlightColor = overlayPillHighlight
                            )

                            // Bottom Info & Connect Button Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    ShimmerBox(
                                        modifier = Modifier
                                            .width(115.dp)
                                            .height(14.dp),
                                        shape = RoundedCornerShape(3.dp),
                                        customBaseColor = overlayTextBase,
                                        customHighlightColor = overlayTextHighlight
                                    )
                                    ShimmerBox(
                                        modifier = Modifier
                                            .width(75.dp)
                                            .height(10.dp),
                                        shape = RoundedCornerShape(3.dp),
                                        customBaseColor = overlayTextBase.copy(alpha = 0.75f),
                                        customHighlightColor = overlayTextHighlight
                                    )
                                    ShimmerBox(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(9.dp),
                                        shape = RoundedCornerShape(3.dp),
                                        customBaseColor = overlayTextBase.copy(alpha = 0.6f),
                                        customHighlightColor = overlayTextHighlight
                                    )
                                }

                                ShimmerBox(
                                    modifier = Modifier
                                        .size(width = 75.dp, height = 26.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    customBaseColor = overlayBtnBase,
                                    customHighlightColor = overlayBtnHighlight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Right peek card (206x165)
                        Box(
                            modifier = Modifier
                                .width(65.dp)
                                .height(165.dp)
                                .offset(x = 8.dp)
                                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                                .background(colors.cardSurface)
                                .padding(8.dp)
                        ) {
                            ShimmerBox(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Recent Uploads Section (Matching RecentUploadCard: 184x262)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section Title Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(140.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(48.dp)
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Cards Row (184x262 exact cards)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    userScrollEnabled = false
                ) {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .width(184.dp)
                                .height(262.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.cardSurface)
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Inner Image Box (128dp, radius 12dp)
                                    ShimmerBox(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(128.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Product Title
                                    ShimmerBox(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(14.dp),
                                        shape = RoundedCornerShape(3.dp)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Seller Row & Price
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            ShimmerCircle(size = 16.dp)
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                ShimmerBox(
                                                    modifier = Modifier
                                                        .width(48.dp)
                                                        .height(9.dp),
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                                ShimmerBox(
                                                    modifier = Modifier
                                                        .width(36.dp)
                                                        .height(7.dp),
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                            }
                                        }

                                        ShimmerBox(
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(16.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    }
                                }

                                // Chat Button (height 36dp, radius 14dp)
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Skeleton layout matching CampusFeedScreen (Filter Pills + 2-Column Grid with 4 identical cards).
 */
@Composable
private fun FeedGridSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = false,
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Filter Pills Row matching FeedFilterPills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val pillWidths = listOf(60.dp, 75.dp, 85.dp, 65.dp, 70.dp, 80.dp)
                items(pillWidths.size) { index ->
                    ShimmerBox(
                        modifier = Modifier
                            .size(width = pillWidths[index], height = 28.dp),
                        shape = RoundedCornerShape(25.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. 2-Column Vertical Grid with 4 identical cards (2x2)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeedGridItemSkeleton(modifier = Modifier.weight(1f))
                    FeedGridItemSkeleton(modifier = Modifier.weight(1f))
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeedGridItemSkeleton(modifier = Modifier.weight(1f))
                    FeedGridItemSkeleton(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Reusable 262dp Feed Card Skeleton matching RecentUploadCard layout.
 */
@Composable
private fun FeedGridItemSkeleton(modifier: Modifier = Modifier) {
    val colors = UniSwapTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(262.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardSurface)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Inner Image Box (128dp, radius 12dp)
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Product Title
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(13.dp),
                    shape = RoundedCornerShape(3.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Seller Row & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShimmerCircle(size = 16.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            ShimmerBox(
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(9.dp),
                                shape = RoundedCornerShape(2.dp)
                            )
                            ShimmerBox(
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(7.dp),
                                shape = RoundedCornerShape(2.dp)
                            )
                        }
                    }

                    ShimmerBox(
                        modifier = Modifier
                            .width(38.dp)
                            .height(14.dp),
                        shape = RoundedCornerShape(3.dp)
                    )
                }
            }

            // Chat Button (height 36dp, radius 14dp)
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

/**
 * Skeleton layout matching MessagesScreen conversation thread rows.
 */
@Composable
private fun MessagesInboxSkeleton(modifier: Modifier = Modifier) {
    val colors = UniSwapTheme.colors

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        userScrollEnabled = false
    ) {
        items(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.cardSurface.copy(alpha = 0.5f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar circle
                ShimmerCircle(size = 50.dp)

                // Content column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        ShimmerBox(
                            modifier = Modifier
                                .width(40.dp)
                                .height(12.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        ShimmerCircle(size = 8.dp)
                    }
                }
            }
        }
    }
}

/**
 * Skeleton layout matching PickupChatScreen message bubbles.
 */
@Composable
private fun ChatThreadSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Incoming bubble (Left)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            ShimmerBox(
                modifier = Modifier
                    .width(220.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
            )
        }

        // Outgoing bubble (Right)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            ShimmerBox(
                modifier = Modifier
                    .width(180.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
            )
        }

        // Incoming bubble (Left)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            ShimmerBox(
                modifier = Modifier
                    .width(260.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
            )
        }

        // Outgoing bubble (Right)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            ShimmerBox(
                modifier = Modifier
                    .width(150.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
            )
        }

        // Incoming bubble (Left)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            ShimmerBox(
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
            )
        }
    }
}
