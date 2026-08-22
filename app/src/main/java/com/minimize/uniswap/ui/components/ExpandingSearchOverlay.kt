package com.minimize.uniswap.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusCategory
import com.minimize.uniswap.ui.theme.*

/**
 * Unified Gmail-style Expanding Search Bar.
 * The textfield and container morph in place:
 * - Collapsed: 50dp capsule alongside 42x42 profile avatar
 * - Expanded: Morphs smoothly into an expansive card with back arrow and 3x2 discovery category tiles
 */
@Composable
fun GmailStyleExpandingSearch(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    categories: List<CampusCategory>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    profilePicUrl: String? = null,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    Surface(
        color = themeColors.cardSurface,
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp,
            bottomStart = if (isExpanded) 40.dp else 26.dp,
            bottomEnd = if (isExpanded) 40.dp else 26.dp
        ),
        shadowElevation = if (isExpanded) 8.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isExpanded) 16.dp else 0.dp,
                    vertical = if (isExpanded) 12.dp else 0.dp
                )
        ) {
            // Main Top Bar Row: [Avatar or Back] + [Text Input] + [Close or Search Icon]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .then(
                        if (!isExpanded) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onExpandedChange(true) }
                            )
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon: Profile Avatar when collapsed <-> Back Button when expanded
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                    },
                    label = "leading_icon_transition"
                ) { expanded ->
                    if (expanded) {
                        IconButton(
                            onClick = { onExpandedChange(false) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = stringResource(R.string.action_back),
                                tint = themeColors.textPrimary,
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(10.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(themeColors.btnBackBg)
                                .clickable(onClick = onProfileClick)
                        ) {
                            AsyncImage(
                                model = profilePicUrl?.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200" }
                                    ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200",
                                contentDescription = stringResource(R.string.field_title_label),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Unified Text Field
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
                        onValueChange = {
                            onQueryChange(it)
                            if (!isExpanded && it.isNotEmpty()) {
                                onExpandedChange(true)
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.textPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(themeColors.textPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Trailing Action: Clear button when text present or 30x30 Search Badge
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_clear_all),
                            tint = themeColors.textSubtle,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(themeColors.textPrimary)
                            .clickable {
                                if (!isExpanded) onExpandedChange(true)
                            },
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

            // Expanded Discovery Content (Animated in place)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayCategories = categories.filter { it.id != "all" }.take(6)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        userScrollEnabled = false
                    ) {
                        items(displayCategories, key = { it.id }) { category ->
                            CategoryTile(
                                category = category,
                                isSelected = category.id == selectedCategoryId,
                                onClick = {
                                    onCategorySelected(category.id)
                                    onExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 86x86 Category Tile matching Figma CSS specs.
 */
@Composable
private fun CategoryTile(
    category: CampusCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) themeColors.textPrimary else themeColors.btnBackBg)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) themeColors.textPrimary else if (themeColors.isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (category.iconUrl.isNotBlank()) {
                AsyncImage(
                    model = category.iconUrl,
                    contentDescription = category.name,
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = category.name,
                    tint = if (isSelected) themeColors.background else themeColors.textPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = (-0.2).sp,
            color = themeColors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}
