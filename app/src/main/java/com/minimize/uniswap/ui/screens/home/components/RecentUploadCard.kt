package com.minimize.uniswap.ui.screens.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.components.UserAvatar
import com.minimize.uniswap.ui.theme.*

/**
 * Recently Uploads Card.
 * Outer card: 192x286, corner radius 15dp, fill #121416.
 * Inner image card: corner radius 11dp, light background.
 * Profile icon: 16x16, Name: 8sp medium, Dept: 6sp regular (#959595), Price: Light matter (INR).
 * Chat button: fill #D9D9D9, corner radius 15dp, text 11sp regular.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentUploadCard(
    item: CampusItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isSaved: Boolean = false,
    onSaveClick: () -> Unit = {},
    onChatClick: () -> Unit = onClick,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors

    Box(
        modifier = modifier
            .width(184.dp)
            .height(262.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(themeColors.cardSurface)
            .border(
                width = if (!themeColors.isDark) 1.dp else 0.dp,
                color = if (!themeColors.isDark) Color(0xFFE5E7EB) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Section (Image + Title + Seller Row)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Inner Image Card (radius 12dp, full crop fill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.btnBackBg),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.imageUrl.ifBlank { item.category.getPlaceholderUrl() },
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Heart Toggle Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f))
                            .clickable(onClick = onSaveClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isSaved) stringResource(R.string.action_unsave_item) else stringResource(R.string.action_save_item),
                            tint = if (isSaved) Color(0xFFFF4B6E) else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Product Title (12sp regular, tight line height)
                Text(
                    text = item.title.ifBlank { stringResource(R.string.sample_recent_title) },
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = themeColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Seller Info & Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar (16x16) + Name (9sp med) + Dept (7sp reg #959595)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(themeColors.btnBackBg)
                        ) {
                            UserAvatar(
                                avatarId = item.sellerAvatarId,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = stringResource(R.string.sample_seller_navneet),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.sp,
                                lineHeight = 11.sp,
                                color = themeColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.sample_dept_robotics),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 7.sp,
                                lineHeight = 9.sp,
                                color = themeColors.textSubtle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Price (Matter Light INR)
                    val priceText = if (item.price == 0.0) {
                        stringResource(R.string.price_free)
                    } else {
                        stringResource(R.string.price_inr_format, item.price.toInt())
                    }
                    Text(
                        text = priceText,
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        color = themeColors.textPrimary
                    )
                }
            }

            // 2. Chat Button (Radius 14dp, Fill #D9D9D9 in dark, #0A0A0A in light)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (themeColors.isDark) BtnChatBg else PaletteLight.Gray950)
                    .clickable(onClick = onChatClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.action_chat),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (themeColors.isDark) PaletteLight.Gray950 else Color.White
                )
            }
        }
    }
}
