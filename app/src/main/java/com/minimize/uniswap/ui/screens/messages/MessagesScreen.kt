package com.minimize.uniswap.ui.screens.messages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.EmptyStateView
import com.minimize.uniswap.ui.components.UserAvatar
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.UniSwapTheme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import com.minimize.uniswap.ui.components.BlockUserDialog
import com.minimize.uniswap.ui.components.ConversationActionBottomSheet
import com.minimize.uniswap.ui.components.MessageReceiptStatus
import com.minimize.uniswap.ui.components.ReportBottomSheet

/**
 * Messages Screen displaying all active chats.
 * Features 42x42 profile avatar, 50dp capsule search with 30x30 circle,
 * 14sp "Messages" section heading, and real-time chat rows with avatars.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagesScreen(
    onConversationClick: (itemId: String, buyerId: String) -> Unit = { _, _ -> },
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyUnread by remember { mutableStateOf(false) }
    val conversations by viewModel.threads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUserFlow.collectAsStateWithLifecycle(initialValue = null)

    var selectedConversationForAction by remember { mutableStateOf<ConversationItemUiModel?>(null) }
    var conversationToDelete by remember { mutableStateOf<ConversationItemUiModel?>(null) }
    var conversationToBlock by remember { mutableStateOf<ConversationItemUiModel?>(null) }
    var conversationToReport by remember { mutableStateOf<ConversationItemUiModel?>(null) }

    val unreadCount = remember(conversations) {
        conversations.count { it.isUnread }
    }

    val filteredConversations = remember(conversations, searchQuery, showOnlyUnread) {
        var list = conversations
        if (showOnlyUnread) {
            list = list.filter { it.isUnread }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true) ||
                        it.itemTitle.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    val themeColors = UniSwapTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = themeColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UniSwapTheme.colors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // 1. Profile Avatar (42x42) + Capsule Search Bar (height 50dp)
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
                        UserAvatar(
                            avatarId = currentUser?.avatarId,
                            modifier = Modifier.fillMaxSize()
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
                                        text = stringResource(com.minimize.uniswap.R.string.search_placeholder),
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = themeColors.textSubtle
                                    )
                                }

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
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

                            // 30x30 Circular Icon
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.textPrimary),
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

                Spacer(modifier = Modifier.height(28.dp))

                // 2. Section Heading Row: "Messages" + Unread Toggle Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.messages_title),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 15.sp,
                            letterSpacing = (-0.28).sp,
                            color = themeColors.textPrimary
                        )

                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(themeColors.wasteMetricGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$unreadCount",
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = themeColors.wasteMetricGreen
                                )
                            }
                        }
                    }

                    // Unread Toggle Filter Pill with Smooth Motion
                    val pillBgColor by animateColorAsState(
                        targetValue = if (showOnlyUnread) themeColors.wasteMetricGreen.copy(alpha = 0.16f) else themeColors.cardSurface,
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                        label = "pill_bg_color"
                    )
                    val pillBorderColor by animateColorAsState(
                        targetValue = if (showOnlyUnread) themeColors.wasteMetricGreen else androidx.compose.ui.graphics.Color.Transparent,
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                        label = "pill_border_color"
                    )
                    val pillTextColor by animateColorAsState(
                        targetValue = if (showOnlyUnread) themeColors.wasteMetricGreen else themeColors.textSubtle,
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                        label = "pill_text_color"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBgColor)
                            .border(
                                width = 1.dp,
                                color = pillBorderColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { showOnlyUnread = !showOnlyUnread }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            AnimatedVisibility(
                                visible = showOnlyUnread,
                                enter = fadeIn(tween(200)) + expandHorizontally(tween(200)),
                                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.wasteMetricGreen)
                                )
                            }
                            Text(
                                text = stringResource(R.string.filter_unread),
                                fontFamily = MatterFontFamily,
                                fontWeight = if (showOnlyUnread) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = pillTextColor
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = when {
                    isLoading -> "LOADING"
                    filteredConversations.isEmpty() -> if (showOnlyUnread && searchQuery.isBlank()) "EMPTY_UNREAD" else if (searchQuery.isBlank()) "EMPTY_INBOX" else "EMPTY_SEARCH"
                    else -> "LIST"
                },
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
                        .togetherWith(fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)))
                },
                label = "messages_screen_content_transition",
                modifier = Modifier.fillMaxSize()
            ) { stateKey ->
                when (stateKey) {
                    "LOADING" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = themeColors.textPrimary,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                    "EMPTY_UNREAD" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateView(
                                title = stringResource(R.string.empty_unread_title),
                                subtitle = stringResource(R.string.empty_unread_subtitle),
                                lottieRes = R.raw.anim_empty_chat,
                                fallbackIcon = Icons.Outlined.ChatBubbleOutline,
                                ctaText = "Show All",
                                onCtaClick = { showOnlyUnread = false },
                                animationSize = 210.dp
                            )
                        }
                    }
                    "EMPTY_INBOX" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateView(
                                title = stringResource(R.string.empty_inbox_title),
                                subtitle = stringResource(R.string.empty_inbox_subtitle),
                                lottieRes = R.raw.anim_empty_chat,
                                fallbackIcon = Icons.Outlined.ChatBubbleOutline,
                                animationSize = 210.dp
                            )
                        }
                    }
                    "EMPTY_SEARCH" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateView(
                                title = stringResource(R.string.empty_inbox_search_title),
                                subtitle = stringResource(R.string.empty_inbox_search_subtitle),
                                lottieRes = R.raw.anim_user_search,
                                fallbackIcon = Icons.Outlined.SearchOff,
                                ctaText = stringResource(R.string.empty_search_cta),
                                onCtaClick = { searchQuery = "" }
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(22.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
                        ) {
                            items(filteredConversations, key = { it.id }) { conversation ->
                                MessageItem(
                                    conversation = conversation,
                                    onClick = { onConversationClick(conversation.itemId, conversation.buyerId) },
                                    onLongClick = { selectedConversationForAction = conversation },
                                    onActionClick = { selectedConversationForAction = conversation },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(220),
                                        fadeOutSpec = tween(180),
                                        placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Action Bottom Sheet for Thread
    selectedConversationForAction?.let { conv ->
        ConversationActionBottomSheet(
            onDismissRequest = { selectedConversationForAction = null },
            displayName = conv.displayName,
            itemTitle = conv.itemTitle,
            onDeleteConversationClick = {
                val target = conv
                selectedConversationForAction = null
                conversationToDelete = target
            },
            onReportClick = {
                val target = conv
                selectedConversationForAction = null
                conversationToReport = target
            },
            onBlockClick = {
                val target = conv
                selectedConversationForAction = null
                conversationToBlock = target
            }
        )
    }

    // Delete Confirmation Dialog
    conversationToDelete?.let { conv ->
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_conversation_confirm_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_conversation_confirm_msg),
                    fontFamily = MatterFontFamily,
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToDelete = conv.id
                        conversationToDelete = null
                        viewModel.deleteConversation(idToDelete)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        fontFamily = MatterFontFamily,
                        color = themeColors.textPrimary
                    )
                }
            },
            containerColor = themeColors.cardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Block User Dialog
    conversationToBlock?.let { conv ->
        val otherUserId = if (viewModel.currentUserId == conv.sellerId) conv.buyerId else conv.sellerId
        BlockUserDialog(
            userName = conv.displayName,
            isBlocking = false,
            onConfirmBlock = {
                viewModel.blockUser(otherUserId) {
                    conversationToBlock = null
                }
            },
            onDismiss = { conversationToBlock = null }
        )
    }

    // Report Sheet
    conversationToReport?.let { conv ->
        ReportBottomSheet(
            onDismissRequest = { conversationToReport = null },
            isSubmitting = false,
            onSubmitReport = { _, _ ->
                conversationToReport = null
            }
        )
    }
}

/**
 * Message Row matching exact Figma CSS tokens.
 * Ellipse 13..20: 41x41 avatar, Name (Matter Medium 14sp), Snippet (Matter Regular 10sp), Time (Matter Regular 8sp).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    conversation: ConversationItemUiModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar (41x41)
        Box(
            modifier = Modifier
                .size(41.dp)
                .clip(CircleShape)
                .background(themeColors.btnBackBg)
        ) {
            UserAvatar(
                avatarId = conversation.avatarUrl,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Center Content: Sender Name + Last Message Snippet
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.displayName,
                    fontFamily = MatterFontFamily,
                    fontWeight = if (conversation.isUnread) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 15.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.textPrimary
                )
                if (conversation.itemTitle.isNotBlank()) {
                    Text(
                        text = " • ${conversation.itemTitle}",
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = themeColors.textSubtle,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (conversation.isLastMessageFromMe) {
                    MessageReceiptStatus(
                        status = conversation.lastMessageStatus,
                        size = 11.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = conversation.lastMessage,
                    fontFamily = MatterFontFamily,
                    fontWeight = if (conversation.isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    letterSpacing = (-0.2).sp,
                    color = if (conversation.isUnread) themeColors.textPrimary else themeColors.textSubtle,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            // Time (Matter Regular 8sp)
            Text(
                text = conversation.timeAgo,
                fontFamily = MatterFontFamily,
                fontWeight = if (conversation.isUnread) FontWeight.Bold else FontWeight.Normal,
                fontSize = 8.sp,
                letterSpacing = (-0.16).sp,
                color = if (conversation.isUnread) themeColors.wasteMetricGreen else themeColors.textSubtle
            )

            if (conversation.isUnread) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(themeColors.wasteMetricGreen)
                )
            }
        }

        // 3-Dot Action Button
        IconButton(
            onClick = onActionClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Actions",
                tint = themeColors.textSubtle,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MessagesScreenPreview() {
    UniSwapTheme {
        MessagesScreen()
    }
}
