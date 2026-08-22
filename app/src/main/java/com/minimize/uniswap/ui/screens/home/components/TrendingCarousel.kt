package com.minimize.uniswap.ui.screens.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.data.model.CampusItem
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * Endless Auto-Scrolling Trending Carousel.
 * Automatically cycles through items with infinite loop support,
 * and scales between 252x200 (center) and 206x165 (adjacent).
 */
@Composable
fun TrendingCarousel(
    items: List<CampusItem>,
    onItemClick: (CampusItem) -> Unit,
    modifier: Modifier = Modifier,
    autoScrollDelayMs: Long = 3500L
) {
    if (items.isEmpty()) return

    // Virtual count for infinite looping
    val virtualCount = Int.MAX_VALUE
    val initialPage = virtualCount / 2 - (virtualCount / 2 % items.size)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { virtualCount })

    // Auto-scroll loop: do not cancel when animation starts
    LaunchedEffect(items.size) {
        if (items.size > 1) {
            while (true) {
                delay(autoScrollDelayMs)
                if (!pagerState.isScrollInProgress) {
                    try {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage + 1,
                            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                        )
                    } catch (_: Exception) {
                        // Resumes on next delay tick
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        val centerCardWidth = 252.dp
        val horizontalPadding = ((maxWidth - centerCardWidth) / 2).coerceAtLeast(0.dp)

        HorizontalPager(
            state = pagerState,
            pageSize = androidx.compose.foundation.pager.PageSize.Fixed(centerCardWidth),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            pageSpacing = (-6).dp,
            beyondViewportPageCount = 2,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val actualIndex = page % items.size
            val item = items[actualIndex]

            // Calculate proximity to center (0.0 = center, 1.0 = adjacent)
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)

            // Scale from 252x200 (center) down towards 206x165 (sides)
            val scale = 1f - (pageOffset * 0.18f)
            val alpha = 1f - (pageOffset * 0.3f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                contentAlignment = Alignment.Center
            ) {
                TrendingCarouselCard(
                    item = item,
                    width = 252.dp,
                    height = 200.dp,
                    onClick = { onItemClick(item) },
                    onConnectClick = { onItemClick(item) }
                )
            }
        }
    }
}
