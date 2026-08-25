package com.minimize.uniswap.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.*
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.minimize.uniswap.ui.components.EmptyStateView

import android.widget.Toast
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalContext
import com.minimize.uniswap.ui.components.BlockUserDialog
import com.minimize.uniswap.ui.components.ItemActionBottomSheet
import com.minimize.uniswap.ui.components.ReportBottomSheet

data class ChatBubbleMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean
)

/**
 * 1-on-1 Chat Interface matching exact Figma CSS tokens.
 * Features 50dp bottom-rounded header, 29x29 avatar, verified student badge,
 * 20dp corner radius message bubbles, and 50dp capsule input bar with "+" button.
 */
@Composable
fun PickupChatScreen(
    itemId: String,
    initialMessage: String? = null,
    buyerId: String? = null,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val item by viewModel.item.collectAsState()
    val liveMessages by viewModel.messages.collectAsState()
    val isSubmittingReport by viewModel.isSubmittingReport.collectAsState()
    val isBlockingUser by viewModel.isBlockingUser.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val blockedUserIds by viewModel.blockedUserIds.collectAsState()

    val context = LocalContext.current
    var inputText by remember(initialMessage) { mutableStateOf(initialMessage ?: "") }
    val listState = rememberLazyListState()

    var isActionSheetOpen by remember { mutableStateOf(false) }
    var isReportSheetOpen by remember { mutableStateOf(false) }
    var isBlockDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(itemId, buyerId) {
        viewModel.loadItem(itemId, buyerId)
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    val isSeller = viewModel.currentUserId.isNotBlank() && viewModel.currentUserId == item?.sellerId
    val otherUserId = if (isSeller) (buyerId ?: "") else (item?.sellerId ?: "")
    val isOtherUserBlocked = otherUserId in blockedUserIds

    val studentName = if (isSeller) {
        "Buyer"
    } else {
        item?.sellerName?.ifBlank { stringResource(R.string.sample_seller_lokesh) }
            ?: stringResource(R.string.sample_seller_lokesh)
    }

    val chatMessages = remember(liveMessages) {
        liveMessages.map {
            ChatBubbleMessage(
                id = it.id,
                text = it.text,
                isFromMe = it.senderId == viewModel.currentUserId
            )
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val themeColors = UniSwapTheme.colors

    // Safety sheets
    if (isActionSheetOpen && item != null) {
        ItemActionBottomSheet(
            onDismissRequest = { isActionSheetOpen = false },
            itemTitle = item!!.title,
            sellerName = studentName,
            isSellerSelf = false,
            onShareClick = {
                val shareText = "Chat regarding: ${item!!.title}"
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share"))
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

    if (isReportSheetOpen && item != null) {
        ReportBottomSheet(
            onDismissRequest = { isReportSheetOpen = false },
            isSubmitting = isSubmittingReport,
            onSubmitReport = { reason, details ->
                viewModel.submitReport(reason, details) { success ->
                    if (success) {
                        isReportSheetOpen = false
                    }
                }
            }
        )
    }

    if (isBlockDialogOpen && item != null) {
        BlockUserDialog(
            userName = studentName,
            isBlocking = isBlockingUser,
            onConfirmBlock = {
                viewModel.blockOtherUser { success ->
                    isBlockDialogOpen = false
                    if (success) {
                        onBackClick()
                    }
                }
            },
            onDismiss = { isBlockDialogOpen = false }
        )
    }

    Scaffold(
        containerColor = themeColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Header: Rectangle 19 (height 143dp, radius 50dp on bottom corners)
            Surface(
                color = themeColors.cardSurface,
                shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(themeColors.btnBackBg)
                                .clickable(onClick = onBackClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = themeColors.textPrimary,
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Avatar (29x29, Ellipse 21)
                        Box(
                            modifier = Modifier
                                .size(29.dp)
                                .clip(CircleShape)
                                .background(themeColors.btnBackBg)
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=100",
                                contentDescription = studentName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Student Name + Verified Student Icon
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = studentName,
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                lineHeight = 15.sp,
                                letterSpacing = (-0.28).sp,
                                color = themeColors.textPrimary
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Verified Badge Icon
                            Icon(
                                painter = painterResource(id = R.drawable.ic_verified),
                                contentDescription = stringResource(R.string.verified_student),
                                tint = VerifiedStudentGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Top Right 3-Dot Safety Action Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(themeColors.btnBackBg)
                                .clickable { isActionSheetOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.action_report_user),
                                tint = themeColors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Bottom Capsule Input Bar with unified navigation bars and IME insets
            Surface(
                color = themeColors.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.union(WindowInsets.ime)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    if (isOtherUserBlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(themeColors.cardSurface)
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.user_blocked_notice),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(themeColors.cardSurface)
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Typing Indicator (|) + Text Input
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.type_here_placeholder),
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            letterSpacing = (-0.2).sp,
                                            color = themeColors.textSubtle
                                        )
                                    }

                                    BasicTextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = MatterFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = themeColors.textPrimary,
                                            fontSize = 13.sp,
                                            letterSpacing = (-0.2).sp
                                        ),
                                        cursorBrush = SolidColor(themeColors.textPrimary),
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences,
                                            imeAction = ImeAction.Send
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (inputText.isNotBlank()) {
                                                    viewModel.sendMessage(inputText)
                                                    inputText = ""
                                                }
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Send Action Button
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (inputText.isNotBlank()) themeColors.textPrimary else themeColors.btnBackBg.copy(alpha = 0.5f))
                                        .clickable(enabled = inputText.isNotBlank()) {
                                            viewModel.sendMessage(inputText)
                                            inputText = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (inputText.isNotBlank()) themeColors.background else themeColors.textSubtle,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (chatMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = stringResource(R.string.empty_chat_title),
                    subtitle = stringResource(R.string.empty_chat_subtitle),
                    lottieRes = R.raw.anim_start_chat,
                    fallbackIcon = Icons.Outlined.Forum,
                    animationSize = 210.dp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
            ) {
                items(chatMessages, key = { it.id }) { message ->
                    ChatBubbleRow(message = message)
                }
            }
        }
    }
}

/**
 * Message Row matching Figma specs:
 * Left incoming with 23x23 avatar + 20dp radius bubble
 * Right outgoing with 20dp radius bubble
 */
@Composable
private fun ChatBubbleRow(
    message: ChatBubbleMessage
) {
    val themeColors = UniSwapTheme.colors

    if (message.isFromMe) {
        // Outgoing message (Right-aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (themeColors.isDark) Color(0xFF22252A) else Color(0xFF171717))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    letterSpacing = (-0.2).sp,
                    color = Color.White
                )
            }
        }
    } else {
        // Incoming message (Left-aligned with 23x23 avatar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // Small participant avatar
            Box(
                modifier = Modifier
                    .size(23.dp)
                    .clip(CircleShape)
                    .background(themeColors.btnBackBg)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=100",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(themeColors.cardSurface)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    letterSpacing = (-0.2).sp,
                    color = themeColors.textPrimary
                )
            }
        }
    }
}
