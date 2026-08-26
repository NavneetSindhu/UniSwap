package com.minimize.uniswap.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.minimize.uniswap.ui.components.UserAvatar
import com.minimize.uniswap.ui.components.nudge.EmailVerificationFlow
import com.minimize.uniswap.ui.components.nudge.VerificationNudgeDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.minimize.uniswap.ui.components.EmptyStateView
import com.minimize.uniswap.ui.theme.*
import java.util.Locale

import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.platform.LocalContext
import com.minimize.uniswap.ui.components.BlockUserDialog
import com.minimize.uniswap.ui.components.ItemActionBottomSheet
import com.minimize.uniswap.ui.components.ReportBottomSheet
import com.minimize.uniswap.ui.components.nudge.GuestNudgeBottomSheet

@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBackClick: () -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onOfferClick: (String, String) -> Unit = { _, _ -> },
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isActionSheetOpen by remember { mutableStateOf(false) }
    var isReportSheetOpen by remember { mutableStateOf(false) }
    var isBlockDialogOpen by remember { mutableStateOf(false) }
    var isGuestNudgeOpen by remember { mutableStateOf(false) }
    var guestNudgeSubtitle by remember { mutableStateOf("") }

    LaunchedEffect(itemId) {
        viewModel.getItem(itemId)
    }

    // Toast feedback for reporting and blocking
    val toastHostState = com.minimize.uniswap.ui.components.LocalToastHostState.current
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            if (msg.contains("failed", ignoreCase = true) || msg.contains("error", ignoreCase = true)) {
                toastHostState.showError(msg)
            } else {
                toastHostState.showSuccess(msg)
            }
            viewModel.clearUserMessage()
        }
    }

    // Email Verification Nudge Barrier
    if (state.showNudge) {
        VerificationNudgeDialog(
            onDismiss = { viewModel.dismissNudge() },
            onVerifyClick = { viewModel.startVerificationFlow() }
        )
    }

    // Email Verification Flow
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

    // Item Action Menu (Share, Report, Block)
    if (isActionSheetOpen && state.item != null) {
        ItemActionBottomSheet(
            onDismissRequest = { isActionSheetOpen = false },
            itemTitle = state.item!!.title,
            sellerName = state.item!!.sellerName,
            isSellerSelf = state.item!!.sellerId == state.currentUserId,
            onShareClick = {
                com.minimize.uniswap.util.ShareUtils.shareProduct(context, state.item!!)
                isActionSheetOpen = false
            },
            onReportClick = {
                isActionSheetOpen = false
                isReportSheetOpen = true
            },
            onBlockClick = {
                isActionSheetOpen = false
                isBlockDialogOpen = true
            }
        )
    }

    // Report Bottom Sheet
    if (isReportSheetOpen && state.item != null) {
        ReportBottomSheet(
            onDismissRequest = { isReportSheetOpen = false },
            isSubmitting = state.isSubmittingReport,
            onSubmitReport = { reason, details ->
                viewModel.submitReport(reason, details) { success ->
                    if (success) {
                        isReportSheetOpen = false
                    }
                }
            }
        )
    }

    // Block User Dialog
    if (isBlockDialogOpen && state.item != null) {
        BlockUserDialog(
            userName = state.item!!.sellerName,
            isBlocking = state.isBlockingSeller,
            onConfirmBlock = {
                viewModel.blockSeller {
                    isBlockDialogOpen = false
                    onBackClick()
                }
            },
            onDismiss = { isBlockDialogOpen = false }
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
                    isSaved = state.isSaved,
                    onSaveClick = {
                        if (state.isGuestMode) {
                            guestNudgeSubtitle = context.getString(R.string.guest_nudge_favorite_subtitle)
                            isGuestNudgeOpen = true
                        } else {
                            viewModel.toggleSaveItem()
                        }
                    },
                    onBackClick = onBackClick,
                    onActionMenuClick = { isActionSheetOpen = true },
                    onChatClick = {
                        if (state.isGuestMode) {
                            guestNudgeSubtitle = context.getString(
                                R.string.guest_nudge_chat_subtitle,
                                state.item?.sellerName ?: "seller"
                            )
                            isGuestNudgeOpen = true
                        } else {
                            onChatClick(state.item!!.id)
                        }
                    },
                    onOfferClick = {
                        if (state.isGuestMode) {
                            guestNudgeSubtitle = context.getString(
                                R.string.guest_nudge_chat_subtitle,
                                state.item?.sellerName ?: "seller"
                            )
                            isGuestNudgeOpen = true
                        } else {
                            val defaultOfferMessage = "Hi! I would like to buy ${state.item!!.title} for ₹${state.item!!.price.toInt()}."
                            onOfferClick(state.item!!.id, defaultOfferMessage)
                        }
                    },
                    onToggleSold = { viewModel.markAsSold() },
                    onDeleteClick = { viewModel.deleteListing { onBackClick() } }
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = stringResource(R.string.item_not_found_title),
                        subtitle = state.error ?: stringResource(R.string.item_not_found_subtitle),
                        fallbackIcon = Icons.Outlined.RemoveShoppingCart,
                        ctaText = stringResource(R.string.item_not_found_cta),
                        onCtaClick = onBackClick
                    )
                }
            }
        }
    }

    if (isGuestNudgeOpen) {
        GuestNudgeBottomSheet(
            onDismissRequest = { isGuestNudgeOpen = false },
            onSignInClick = {
                isGuestNudgeOpen = false
                onSignInClick()
            },
            onSignUpClick = {
                isGuestNudgeOpen = false
                onSignUpClick()
            },
            subtitle = guestNudgeSubtitle
        )
    }
}

@Composable
private fun ItemDetailsContent(
    item: CampusItem,
    currentUserId: String,
    isSaved: Boolean = false,
    onSaveClick: () -> Unit = {},
    onBackClick: () -> Unit,
    onActionMenuClick: () -> Unit,
    onChatClick: () -> Unit,
    onOfferClick: () -> Unit,
    onToggleSold: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isSeller = item.sellerId == currentUserId
    val themeColors = UniSwapTheme.colors

    val allImages = remember(item) { item.getAllImages() }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { allImages.size })
    var isPreviewOpen by remember { mutableStateOf(false) }
    var previewSelectedPage by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // 1. Top Component: Full-Bleed Product Showcase Header Carousel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(410.dp)
                .background(themeColors.cardSurface)
        ) {
            // Product Showcase HorizontalPager (Full bleed crop fill)
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = allImages[page],
                    contentDescription = "${item.title} ($page)",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            previewSelectedPage = page
                            isPreviewOpen = true
                        },
                    contentScale = ContentScale.Crop
                )
            }

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
                    .align(Alignment.TopStart)
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

            // Top Right Circular Action Buttons (Heart + 3-Dot)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(end = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart Favorite Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.btnBackBg)
                        .clickable(onClick = onSaveClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) stringResource(R.string.action_unsave_item) else stringResource(R.string.action_save_item),
                        tint = if (isSaved) Color(0xFFFF4B6E) else themeColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3-Dot More Menu
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.btnBackBg)
                        .clickable(onClick = onActionMenuClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.action_share_listing),
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Dot Indicator (Centered under image, dynamic dots with 8dp gap)
            if (allImages.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(allImages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PagerDotActive else PagerDotInactive)
                        )
                    }
                }
            }
        }

        // Fullscreen Image Preview Dialog
        if (isPreviewOpen) {
            FullScreenImagePreviewDialog(
                images = allImages,
                initialPage = previewSelectedPage,
                onDismiss = { isPreviewOpen = false }
            )
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
                        UserAvatar(
                            avatarId = item.sellerAvatarId,
                            modifier = Modifier.fillMaxSize()
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

/**
 * Fullscreen immersive image gallery preview with swipeable HorizontalPager,
 * top close (X) button, and dynamic page indicator.
 */
@Composable
fun FullScreenImagePreviewDialog(
    images: List<String>,
    initialPage: Int = 0,
    onDismiss: () -> Unit
) {
    val previewPagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
        pageCount = { images.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Horizontal Pager for swiping through all images in fullscreen
            androidx.compose.foundation.pager.HorizontalPager(
                state = previewPagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = images[page],
                        contentDescription = "Image preview $page",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Top Overlay Bar: Page Counter & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Counter Badge (e.g., "1 of 4")
                if (images.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = stringResource(
                                R.string.image_preview_counter,
                                previewPagerState.currentPage + 1,
                                images.size
                            ),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.image_preview_close),
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom Dot Indicator if multiple images
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(bottom = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(images.size) { index ->
                        val isSelected = previewPagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.35f))
                        )
                    }
                }
            }
        }
    }
}


