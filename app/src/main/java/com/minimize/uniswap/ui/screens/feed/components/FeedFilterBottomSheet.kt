package com.minimize.uniswap.ui.screens.feed.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.AppBottomSheet
import com.minimize.uniswap.ui.theme.*

/**
 * Supported feed sort dimensions.
 */
enum class FeedSortOption {
    NEWEST,
    TRENDING,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW;

    fun getDisplayNameRes(): Int = when (this) {
        NEWEST -> R.string.feed_sort_newest
        TRENDING -> R.string.feed_sort_trending
        PRICE_LOW_TO_HIGH -> R.string.feed_sort_price_low_high
        PRICE_HIGH_TO_LOW -> R.string.feed_sort_price_high_low
    }
}

/**
 * Supported campus scopes for feed partitioning.
 */
enum class CampusScope {
    MY_CAMPUS,
    ALL_CAMPUSES
}

/**
 * Filter and Sort bottom sheet allowing users to refine the campus marketplace feed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedFilterBottomSheet(
    onDismissRequest: () -> Unit,
    campusScope: CampusScope,
    onCampusScopeChanged: (CampusScope) -> Unit,
    campusName: String? = null,
    selectedSort: FeedSortOption,
    onSortSelected: (FeedSortOption) -> Unit,
    selectedCondition: String?,
    onConditionSelected: (String?) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit,
    freeOnly: Boolean,
    onFreeOnlyChanged: (Boolean) -> Unit,
    verifiedOnly: Boolean,
    onVerifiedOnlyChanged: (Boolean) -> Unit,
    onResetAll: () -> Unit,
    activeFilterCount: Int
) {
    val themeColors = UniSwapTheme.colors

    var tempCampusScope by remember(campusScope) { mutableStateOf(campusScope) }
    var tempSort by remember(selectedSort) { mutableStateOf(selectedSort) }
    var tempCondition by remember(selectedCondition) { mutableStateOf(selectedCondition) }
    var tempPriceRange by remember(priceRange) { mutableStateOf(priceRange) }
    var tempFreeOnly by remember(freeOnly) { mutableStateOf(freeOnly) }
    var tempVerifiedOnly by remember(verifiedOnly) { mutableStateOf(verifiedOnly) }

    val conditions = listOf(
        Pair(null, stringResource(R.string.filter_condition_any)),
        Pair("Brand New", stringResource(R.string.filter_condition_brand_new)),
        Pair("Like New", stringResource(R.string.filter_condition_like_new)),
        Pair("Good", stringResource(R.string.filter_condition_good)),
        Pair("Fair", stringResource(R.string.filter_condition_fair))
    )

    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        heightFraction = 0.85f,
        containerColor = themeColors.cardSurface,
        contentColor = themeColors.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // 1. Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.filter_sheet_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = themeColors.textPrimary
                )

                TextButton(
                    onClick = {
                        tempCampusScope = CampusScope.MY_CAMPUS
                        tempSort = FeedSortOption.NEWEST
                        tempCondition = null
                        tempPriceRange = 0f..10000f
                        tempFreeOnly = false
                        tempVerifiedOnly = false
                        onResetAll()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.filter_reset_all),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = themeColors.textSecondary
                    )
                }
            }

            // 2. Scrollable Filter Form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Section 0: Campus Scope
                Text(
                    text = stringResource(R.string.filter_campus_scope),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val myCampusDisplay = campusName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.feed_scope_my_campus)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipPill(
                        text = myCampusDisplay,
                        isSelected = tempCampusScope == CampusScope.MY_CAMPUS,
                        onClick = { tempCampusScope = CampusScope.MY_CAMPUS },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChipPill(
                        text = stringResource(R.string.feed_scope_all_campuses),
                        isSelected = tempCampusScope == CampusScope.ALL_CAMPUSES,
                        onClick = { tempCampusScope = CampusScope.ALL_CAMPUSES },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section A: Sort By
                Text(
                    text = stringResource(R.string.filter_sort_by),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeedSortOption.values().take(2).forEach { option ->
                        FilterChipPill(
                            text = stringResource(option.getDisplayNameRes()),
                            isSelected = tempSort == option,
                            onClick = { tempSort = option },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeedSortOption.values().drop(2).forEach { option ->
                        FilterChipPill(
                            text = stringResource(option.getDisplayNameRes()),
                            isSelected = tempSort == option,
                            onClick = { tempSort = option },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = themeColors.divider.copy(alpha = 0.5f), thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // Section B: Item Condition
                Text(
                    text = stringResource(R.string.filter_item_condition),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    conditions.take(3).forEach { (value, label) ->
                        FilterChipPill(
                            text = label,
                            isSelected = tempCondition == value,
                            onClick = { tempCondition = value },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    conditions.drop(3).forEach { (value, label) ->
                        FilterChipPill(
                            text = label,
                            isSelected = tempCondition == value,
                            onClick = { tempCondition = value },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = themeColors.divider.copy(alpha = 0.5f), thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // Section C: Price Range Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.filter_price_range),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = themeColors.textPrimary
                    )

                    val maxPlus = if (tempPriceRange.endInclusive >= 10000f) "+" else ""
                    Text(
                        text = stringResource(
                            R.string.filter_price_format,
                            tempPriceRange.start.toInt(),
                            tempPriceRange.endInclusive.toInt(),
                            maxPlus
                        ),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = themeColors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                RangeSlider(
                    value = tempPriceRange,
                    onValueChange = { tempPriceRange = it },
                    valueRange = 0f..10000f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = themeColors.textPrimary,
                        activeTrackColor = themeColors.textPrimary,
                        inactiveTrackColor = themeColors.divider
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = themeColors.divider.copy(alpha = 0.5f), thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Section D: Quick Toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { tempFreeOnly = !tempFreeOnly }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.filter_free_only),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = themeColors.textPrimary
                    )
                    Switch(
                        checked = tempFreeOnly,
                        onCheckedChange = { tempFreeOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeColors.background,
                            checkedTrackColor = themeColors.textPrimary,
                            uncheckedThumbColor = themeColors.textSubtle,
                            uncheckedTrackColor = themeColors.btnBackBg
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { tempVerifiedOnly = !tempVerifiedOnly }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.filter_verified_only),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = themeColors.textPrimary
                    )
                    Switch(
                        checked = tempVerifiedOnly,
                        onCheckedChange = { tempVerifiedOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeColors.background,
                            checkedTrackColor = themeColors.textPrimary,
                            uncheckedThumbColor = themeColors.textSubtle,
                            uncheckedTrackColor = themeColors.btnBackBg
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Apply Action Button
            val dismissSheet = com.minimize.uniswap.ui.components.LocalBottomSheetDismiss.current
            Button(
                onClick = {
                    onCampusScopeChanged(tempCampusScope)
                    onSortSelected(tempSort)
                    onConditionSelected(tempCondition)
                    onPriceRangeChanged(tempPriceRange)
                    onFreeOnlyChanged(tempFreeOnly)
                    onVerifiedOnlyChanged(tempVerifiedOnly)
                    dismissSheet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.textPrimary,
                    contentColor = themeColors.background
                )
            ) {
                Text(
                    text = if (activeFilterCount > 0) {
                        stringResource(R.string.filter_apply_with_count, activeFilterCount)
                    } else {
                        stringResource(R.string.filter_apply_btn)
                    },
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = themeColors.background
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Capsule Chip Pill for selecting sort & condition options.
 */
@Composable
private fun FilterChipPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors

    val bg by animateColorAsState(
        targetValue = if (isSelected) themeColors.textPrimary else themeColors.btnBackBg,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "FilterChipBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) themeColors.background else themeColors.textPrimary,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "FilterChipTextColor"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = MatterFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1
        )
    }
}

// ==========================================
// Previews
// ==========================================

@Preview(name = "Filter Sheet - Dark", showBackground = true, backgroundColor = 0xFF121416)
@Composable
private fun FeedFilterBottomSheetDarkPreview() {
    UniSwapTheme(themeMode = com.minimize.uniswap.data.preferences.ThemeMode.DARK) {
        FeedFilterBottomSheet(
            onDismissRequest = {},
            campusScope = CampusScope.MY_CAMPUS,
            onCampusScopeChanged = {},
            campusName = "USAR GGSIPU",
            selectedSort = FeedSortOption.TRENDING,
            onSortSelected = {},
            selectedCondition = "Brand New",
            onConditionSelected = {},
            priceRange = 0f..5000f,
            onPriceRangeChanged = {},
            freeOnly = false,
            onFreeOnlyChanged = {},
            verifiedOnly = true,
            onVerifiedOnlyChanged = {},
            onResetAll = {},
            activeFilterCount = 3
        )
    }
}
