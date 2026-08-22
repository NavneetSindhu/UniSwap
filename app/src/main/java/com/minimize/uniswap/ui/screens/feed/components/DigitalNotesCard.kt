package com.minimize.uniswap.ui.screens.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.theme.*

/**
 * Digital Study Material / Notes Card for All Feed screen.
 * Dimensions: 191x198, corner radius 15dp, fill #121416.
 * Features preview thumbnail, 10sp title, 10sp PDF count, 14sp INR price, and 92x24 Chat button (radius 15).
 */
@Composable
fun DigitalNotesCard(
    item: CampusItem,
    onClick: () -> Unit,
    onChatClick: () -> Unit = onClick,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(198.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(themeColors.cardSurface)
            .border(
                width = if (!themeColors.isDark) 1.dp else 0.dp,
                color = if (!themeColors.isDark) Color(0xFFE5E7EB) else Color.Transparent,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Image Preview Thumbnail (radius 12dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(if (themeColors.isDark) ContainerHeroDark else PaletteLight.Gray200),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.imageUrl.ifBlank { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=400" },
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 2. Middle Content (Title + Tag)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                Text(
                    text = item.title.ifBlank { stringResource(R.string.sample_notes_title) },
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    letterSpacing = (-0.2).sp,
                    color = themeColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(R.string.sample_notes_pdf_count),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = (-0.2).sp,
                    color = themeColors.textSubtle
                )
            }

            // 3. Bottom Row: Chat Button (92x24) + INR Price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat Button (92x24, radius 15)
                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (themeColors.isDark) BtnChatBg else PaletteLight.Gray950)
                        .clickable(onClick = onChatClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.action_chat),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        color = if (themeColors.isDark) PaletteLight.Gray950 else Color.White
                    )
                }

                // Price (Matter Light 14sp, INR formatted)
                val priceText = if (item.price == 0.0) {
                    stringResource(R.string.price_free)
                } else {
                    stringResource(R.string.price_inr_format, item.price.toInt())
                }
                Text(
                    text = priceText,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.textPrimary
                )
            }
        }
    }
}
