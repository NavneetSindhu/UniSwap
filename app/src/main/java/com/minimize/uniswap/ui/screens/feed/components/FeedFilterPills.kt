package com.minimize.uniswap.ui.screens.feed.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.data.model.CampusCategory
import com.minimize.uniswap.ui.theme.BtnChatWithSeller
import com.minimize.uniswap.ui.theme.BtnBackBg
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.TextMutedLight

/**
 * Filter Pills row for All Feed screen.
 * Dimensions: height 28dp, corner radius 25dp pill.
 * Features smooth color & scale transition on selection change.
 */
@Composable
fun FeedFilterPills(
    categories: List<CampusCategory>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = com.minimize.uniswap.ui.theme.UniSwapTheme.colors

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            val isSelected = category.id.equals(selectedCategoryId, ignoreCase = true)

            val pillBg by animateColorAsState(
                targetValue = if (isSelected) themeColors.textPrimary else themeColors.cardSurface,
                animationSpec = tween(durationMillis = 250),
                label = "pill_bg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) themeColors.background else themeColors.textSubtle,
                animationSpec = tween(durationMillis = 250),
                label = "pill_text"
            )

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(pillBg)
                    .clickable { onCategorySelected(category.id) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = (-0.2).sp,
                    color = textColor
                )
            }
        }
    }
}
