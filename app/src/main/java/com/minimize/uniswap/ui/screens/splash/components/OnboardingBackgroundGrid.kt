package com.minimize.uniswap.ui.screens.splash.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.PaletteDark
import com.minimize.uniswap.ui.theme.PaletteLight

@Composable
fun OnboardingBackgroundGrid(
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 1. Grid of showcase cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-30).dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Textbooks & Study Notes Card
                ShowcaseCard(
                    imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=400",
                    height = 200.dp
                )
                // Backpack & Campus Gear Card
                ShowcaseCard(
                    imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?q=80&w=400",
                    height = 220.dp
                )
            }

            // Center / Main Column (Features DROP Header Card)
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .offset(y = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main Featured Drop Card (Laptop & Electronics)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(PaletteLight.Gray200)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.brand_logo),
                            contentDescription = stringResource(R.string.app_name),
                            tint = PaletteLight.Gray950,
                            modifier = Modifier.height(18.dp)
                        )

                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=600",
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Headphones & Audio Gear Card
                ShowcaseCard(
                    imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=400",
                    height = 180.dp
                )
            }

            // Right Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-15).dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Scientific Calculator & Tech Tools Card
                ShowcaseCard(
                    imageUrl = "https://images.unsplash.com/photo-1587145820266-a5951ee6f620?q=80&w=400",
                    height = 210.dp
                )
                // Sneaker & Campus Lifestyle Card
                ShowcaseCard(
                    imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=400",
                    height = 190.dp
                )
            }
        }

        // 2. Seamless Gradient Overlay Fading to Base Theme Color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            backgroundColor.copy(alpha = 0.4f),
                            backgroundColor.copy(alpha = 0.85f),
                            backgroundColor,
                            backgroundColor
                        )
                    )
                )
        )
    }
}

@Composable
private fun ShowcaseCard(
    imageUrl: String,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(28.dp))
            .background(PaletteDark.Gray100)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
