package com.minimize.uniswap.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.MatterFontFamily

/**
 * Screen-specific Top Header for Home Screen.
 * Features centered DROP logo, 42x42 profile avatar, and capsule search bar with 30x30 circle.
 */
@Composable
fun HomeTopHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    profilePicUrl: String?,
    onProfileClick: () -> Unit,
    onSearchActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeColors = com.minimize.uniswap.ui.theme.UniSwapTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Centered DROP Brand Logo from res/drawable/brand_logo.xml
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.brand_logo),
                contentDescription = "DROP Logo",
                tint = themeColors.textPrimary,
                modifier = Modifier.height(13.dp)
            )
        }

        // 2. Profile Avatar (42x42) + Capsule Search Bar
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
                    model = profilePicUrl?.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200" },
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
                            onValueChange = onQueryChange,
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

                    // Circle Inside TextField (30x30)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(themeColors.textPrimary)
                            .clickable(onClick = onSearchActionClick),
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
    }
}
