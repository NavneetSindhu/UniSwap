package com.minimize.uniswap.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.PaletteLight
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Trending Carousel Card.
 * Main focused dimensions: 252x200 (Corner radius 24dp).
 * Side peek dimensions: 206x165.
 * Features price badge, title, dept, and Connect button.
 */
@Composable
fun TrendingCarouselCard(
    item: CampusItem,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors
    val cornerRadius = if (width >= 240.dp) 24.dp else 20.dp

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(themeColors.cardSurface)
            .border(
                width = if (!themeColors.isDark) 1.dp else 0.dp,
                color = if (!themeColors.isDark) Color(0xFFE5E7EB) else Color.Transparent,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(onClick = onClick)
    ) {
        // 1. Full-Bleed Product Image
        AsyncImage(
            model = item.imageUrl.ifBlank { item.category.getPlaceholderUrl() },
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Bottom Warm/Dark Gradient Overlay for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 60f
                    )
                )
        )

        // 3. Top-Left Price Container (74x24, INR format)
        Box(
            modifier = Modifier
                .padding(top = 12.dp, start = 12.dp)
                .size(width = 74.dp, height = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.50f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            val priceText = if (item.price == 0.0) {
                stringResource(R.string.price_free)
            } else {
                stringResource(R.string.price_inr_format, item.price.toInt())
            }
            Text(
                text = priceText,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White
            )
        }

        // 4. Bottom Info & Connect Button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Column (Title + Dept + Timestamp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = item.title.ifBlank { stringResource(R.string.sample_trending_title) },
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = stringResource(R.string.sample_dept_robotics),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = stringResource(R.string.sample_4_days_ago),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Connect Button (76x26, radius 25 pill)
                Box(
                    modifier = Modifier
                        .size(width = 76.dp, height = 26.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(25.dp))
                        .clickable(onClick = onConnectClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.action_connect),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

