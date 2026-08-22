package com.minimize.uniswap.ui.screens.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.DotIndicator
import com.minimize.uniswap.ui.screens.auth.LoginViewModel
import com.minimize.uniswap.ui.screens.splash.components.OnboardingBackgroundGrid
import com.minimize.uniswap.ui.screens.splash.components.WelcomeAuthCard
import com.minimize.uniswap.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onNavigateToSignUp: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pageTitles = remember {
        listOf(
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
        )
    }

    val totalPages = 4 // 3 value-prop slides + 1 Welcome/Auth slide
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val isAuthSlide = pagerState.currentPage == totalPages - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaletteDark.Base)
    ) {
        // 1. Static Showcase Background Grid (Only visible during onboarding value-prop slides 1, 2, 3)
        AnimatedVisibility(
            visible = !isAuthSlide,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            OnboardingBackgroundGrid()
        }

        if (isAuthSlide) {
            // Full-screen Clean Auth Experience (iPhone 16 & 17 Pro - 11)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WelcomeAuthCard(
                    email = viewModel.email,
                    onEmailChange = { viewModel.email = it },
                    password = viewModel.password,
                    onPasswordChange = { viewModel.password = it },
                    name = viewModel.name,
                    onNameChange = { viewModel.name = it },
                    isSignUpMode = viewModel.isSignUpMode,
                    onToggleMode = { onNavigateToSignUp() },
                    onSubmit = { viewModel.onSubmitClick() },
                    onGoogleSignInClick = { viewModel.onGoogleLoginClick(context) },
                    isLoading = viewModel.isLoading,
                    errorMessage = viewModel.errorMessage
                )
            }
        } else {
            // Top Header: Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.action_skip),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(totalPages - 1)
                            }
                        }
                        .padding(8.dp)
                )
            }

            // Bottom Content Container for Onboarding Slides (1..3)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fixed-Height Pager Content Area
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) { page ->
                    if (page < pageTitles.size) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                text = stringResource(pageTitles[page]),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                lineHeight = 42.sp,
                                letterSpacing = (-0.5).sp,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Dot Indicator (Slides 1..3)
                DotIndicator(
                    pageCount = 3,
                    currentPage = pagerState.currentPage.coerceAtMost(2),
                    activeColor = Color.White,
                    inactiveColor = Color.White.copy(alpha = 0.25f)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Continue Button
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PaletteLight.Gray950
                    )
                ) {
                    Text(
                        text = stringResource(if (pagerState.currentPage == 2) R.string.action_get_started else R.string.action_continue),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PaletteLight.Gray950
                    )
                }
            }
        }
    }

    // Trigger onComplete when authentication succeeds
    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            onComplete()
        }
    }
}