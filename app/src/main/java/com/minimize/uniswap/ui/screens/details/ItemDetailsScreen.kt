package com.minimize.uniswap.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.ui.components.DotIndicator
import com.minimize.uniswap.ui.components.nudge.EmailVerificationFlow
import com.minimize.uniswap.ui.components.nudge.VerificationNudgeDialog
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.minimize.uniswap.ui.theme.*
import java.util.Locale

@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBackClick: () -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onOfferClick: (String, String) -> Unit = { _, _ -> },
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.getItem(itemId)
    }

    // Email Verification Nudge Barrier
    if (state.showNudge) {
        VerificationNudgeDialog(
            onDismiss = { viewModel.dismissNudge() },
            onVerifyClick = { viewModel.startVerificationFlow() }
        )
    }

    if (state.showVerificationFlow) {
        EmailVerificationFlow(
            email = state.userEmail,
            onSendEmail = { viewModel.sendVerificationEmail() },
            onCheckStatus = { viewModel.checkVerificationStatus() },
            isProcessing = state.isProcessingVerification,
            isSent = state.isVerificationSent,
            isVerified = state.isEmailVerified,
            onDismiss = { viewModel.dismissNudge() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            state.item != null -> {
                ItemDetailsContent(
                    item = state.item!!,
                    currentUserId = state.currentUserId,
                    onBackClick = onBackClick,
                    onChatClick = {
                        onChatClick(state.item!!.id)
                    },
                    onOfferClick = {
                        val defaultOfferMessage = "Hi! I would like to buy ${state.item!!.title} for ₹${state.item!!.price.toInt()}."
                        onOfferClick(state.item!!.id, defaultOfferMessage)
                    },
                    onToggleSold = { viewModel.markAsSold() },
                    onDeleteClick = { viewModel.deleteListing { onBackClick() } }
                )
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.action_back))
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemDetailsContent(
    item: CampusItem,
    currentUserId: String,
    onBackClick: () -> Unit,
    onChatClick: () -> Unit,
    onOfferClick: () -> Unit,
    onToggleSold: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isSeller = item.sellerId == currentUserId

    val themeColors = UniSwapTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // 1. Top Component: Full-Bleed Product Showcase Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(410.dp)
                .background(themeColors.cardSurface)
        ) {
            // Product Showcase Image (Full bleed crop fill)
            AsyncImage(
                model = item.imageUrl.ifBlank { item.category.getPlaceholderUrl() },
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top subtle gradient scrim for back button contrast
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Top Left Circular Back Button (Ellipse 12: 38x38)
            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(start = 24.dp, top = 8.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(themeColors.btnBackBg)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
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

            // Dot Indicator (Centered under shoe, 6dp dots with 12dp gap)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) PagerDotActive else PagerDotInactive)
                    )
                }
            }
        }

        // 2. Bottom Component: Overlapping Card with 50dp Top Rounded Corners
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 370.dp)
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(themeColors.cardSurface)
        ) {
            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                // Title & Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Product Title
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title.ifBlank { stringResource(R.string.sample_details_title) },
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 26.sp,
                            lineHeight = 32.sp,
                            letterSpacing = (-0.5).sp,
                            color = themeColors.textPrimary
                        )
                        if (item.status == com.minimize.uniswap.data.model.ItemStatus.SOLD) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.status_sold_badge),
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Price: INR Format
                    val priceText = if (item.price == 0.0) {
                        stringResource(R.string.price_free)
                    } else {
                        stringResource(R.string.price_inr_format, item.price.toInt())
                    }
                    Text(
                        text = priceText,
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 26.sp,
                        letterSpacing = (-0.48).sp,
                        color = themeColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Description of product Heading
                Text(
                    text = stringResource(R.string.description_header),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    letterSpacing = (-0.2).sp,
                    color = themeColors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Description Body
                Text(
                    text = item.description.ifBlank {
                        stringResource(R.string.sample_details_description)
                    },
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    letterSpacing = (-0.2).sp,
                    color = themeColors.textSecondary
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Seller Profile Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar (34x34, Circle)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(themeColors.btnBackBg)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150",
                            contentDescription = "Seller Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Name + Verified Badge + Campus
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.sellerName.ifBlank { stringResource(R.string.sample_seller_lokesh) },
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                letterSpacing = (-0.24).sp,
                                color = themeColors.textPrimary
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Icon(
                                painter = painterResource(id = R.drawable.ic_verified),
                                contentDescription = "Verified Badge",
                                tint = VerifiedStudentGreen,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = stringResource(R.string.sample_campus_pu23),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 8.sp,
                            letterSpacing = (-0.16).sp,
                            color = TextMutedLight
                        )
                    }

                    // Verified Student Text on Right (8sp, #02B014)
                    Text(
                        text = stringResource(R.string.verified_student),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 8.sp,
                        letterSpacing = (-0.16).sp,
                        color = VerifiedStudentGreen
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.delete_listing_confirm_title),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.delete_listing_confirm_msg),
                            fontFamily = MatterFontFamily,
                            color = themeColors.textSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                onDeleteClick()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_delete),
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(
                                text = stringResource(R.string.action_cancel),
                                color = themeColors.textPrimary
                            )
                        }
                    },
                    containerColor = themeColors.cardSurface
                )
            }

            // Fixed Bottom Action Buttons Container
            if (isSeller) {
                SellerBottomActionBar(
                    isSold = item.status == com.minimize.uniswap.data.model.ItemStatus.SOLD,
                    onToggleSold = onToggleSold,
                    onDeleteClick = { showDeleteDialog = true }
                )
            } else {
                StickyBottomActionBar(
                    onChatClick = onChatClick,
                    onOfferClick = onOfferClick
                )
            }
        }
    }
}

@Composable
private fun SellerBottomActionBar(
    isSold: Boolean,
    onToggleSold: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val themeColors = UniSwapTheme.colors

    Surface(
        color = themeColors.cardSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onToggleSold,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSold) Color(0xFF10B981) else themeColors.btnBackBg,
                    contentColor = if (isSold) Color.White else themeColors.textPrimary
                )
            ) {
                Text(
                    text = stringResource(if (isSold) R.string.action_mark_available else R.string.action_mark_sold),
                    fontFamily = MatterFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.28).sp
                )
            }

            Button(
                onClick = onDeleteClick,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Text(
                    text = stringResource(R.string.action_delete_listing),
                    fontFamily = MatterFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.28).sp
                )
            }
        }
    }
}

@Composable
private fun StickyBottomActionBar(
    onChatClick: () -> Unit,
    onOfferClick: () -> Unit
) {
    val themeColors = UniSwapTheme.colors

    Surface(
        color = themeColors.cardSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1: "Chat with seller" (Rectangle 33: 50dp height, #22252A, radius 25)
            Button(
                onClick = onChatClick,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BtnChatWithSeller,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.chat_with_seller),
                    fontFamily = MatterFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.28).sp
                )
            }

            // Button 2: "Make offer" (Rectangle 34: 50dp height, #59626F, radius 25)
            Button(
                onClick = onOfferClick,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BtnMakeOffer,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.make_offer),
                    fontFamily = MatterFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.28).sp
                )
            }
        }
    }
}


