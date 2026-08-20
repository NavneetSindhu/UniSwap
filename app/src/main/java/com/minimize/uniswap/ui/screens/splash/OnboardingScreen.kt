package com.minimize.uniswap.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.AppPrimaryButton
import com.minimize.uniswap.ui.components.DotIndicator
import com.minimize.uniswap.ui.theme.OnboardingOverlay
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val backgroundImageUrl: String
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPageData("Buy, sell and exchange with students around you.", "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?q=80&w=1000"),
            OnboardingPageData("Verified students. Local deals. Safer meetups.", "https://images.unsplash.com/photo-1523240795612-9a054b0db644?q=80&w=1000"),
            OnboardingPageData("List it. Find a buyer.\nKeep it moving.", "https://images.unsplash.com/photo-1519452635265-7b1fbfd1e4e0?q=80&w=1000")
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // 1. Fullscreen Background Image
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = pages[page].backgroundImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 16.dp),
                contentScale = ContentScale.Crop
            )
        }

        // 2. Top Header (Logo as AsyncImage & Skip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "DROP" Logo as AsyncImage
            AsyncImage(
                model = "URL_TO_YOUR_DROP_LOGO_IMAGE",
                contentDescription = "Drop Logo",
                modifier = Modifier.height(24.dp), // Adjust based on original logo ratio
                contentScale = ContentScale.Fit
            )

            Text(
                text = stringResource(R.string.action_skip),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f)
                ),
                modifier = Modifier.clickable(onClick = onComplete)
            )
        }

        // 3. Fixed Bottom Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f) // Responsive height instead of hardcoded 489
                .align(Alignment.BottomCenter)
                .clip(MaterialTheme.shapes.extraLarge) // 56.dp radius from Shape.kt
                .background(OnboardingOverlay) // #191919 @ 20%
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Swiping Text Layer
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    userScrollEnabled = false
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = pages[page].title,
                            // Uses Matter, 32sp, 105% LH, -2% LS from Type.kt
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }

                // Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DotIndicator(
                        pageCount = pages.size,
                        currentPage = pagerState.currentPage
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AppPrimaryButton(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Continue",
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            } else {
                                onComplete()
                            }
                        }
                    )
                }
            }
        }
    }
}