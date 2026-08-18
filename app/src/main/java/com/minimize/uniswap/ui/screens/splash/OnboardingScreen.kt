package com.minimize.uniswap.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.AppBottomSheet
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val titleRes: Int,
    val backgroundImageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                titleRes = R.string.onboarding_title_1,
                backgroundImageUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?q=80&w=1000"
            ),
            OnboardingPageData(
                titleRes = R.string.onboarding_title_2,
                backgroundImageUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?q=80&w=1000"
            ),
            OnboardingPageData(
                titleRes = R.string.onboarding_title_3,
                backgroundImageUrl = "https://images.unsplash.com/photo-1519452635265-7b1fbfd1e4e0?q=80&w=1000"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2C3E50), Color(0xFF0D0E11))
                )
            )
    ) {
        // 1. Fullscreen Background Image Stream
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false // Controlled via the bottom sheet pager
        ) { page ->
            AsyncImage(
                model = pages[page].backgroundImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 24.dp),
                contentScale = ContentScale.Crop
            )
        }

        // 2. Top Header (Logo & Skip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White
                )
            )

            Text(
                text = stringResource(R.string.action_skip),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                ),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onComplete
                )
            )
        }

        // 3. Persistent 0.6f AppBottomSheet Container
        AppBottomSheet(
            onDismissRequest = onComplete,
            showCloseIcon = false, // Keep onboarding clean, user uses Skip or Get Started
            containerColor = Color(0xFF141518)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive Pager for Text
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = stringResource(pages[page].titleRes),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                // Dot Indicators & Action Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 18.dp else 6.dp,
                                label = "dot_width"
                            )

                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White
                                        else Color.White.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Continue / Get Started Button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF111827)
                        )
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) {
                                stringResource(R.string.action_get_started)
                            } else {
                                stringResource(R.string.action_continue)
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}