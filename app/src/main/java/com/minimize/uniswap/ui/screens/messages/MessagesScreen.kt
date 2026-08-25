package com.minimize.uniswap.ui.screens.messages

import androidx.compose.foundation.background
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

/**
 * Messages Screen displaying all active chats.
 * Features 42x42 profile avatar, 50dp capsule search with 30x30 circle,
 * 14sp "Messages" section heading, and real-time chat rows with avatars.
 */
@Composable
fun MessagesScreen(
    onConversationClick: (itemId: String, buyerId: String) -> Unit = { _, _ -> },
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val conversations by viewModel.threads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUserFlow.collectAsStateWithLifecycle(initialValue = null)

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.lastMessage.contains(searchQuery, ignoreCase = true) ||
                    it.itemTitle.contains(searchQuery, ignoreCase = true)
        }
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

                // 2. Section Heading: "Messages" (Matter Medium 14sp)
                Text(
                    text = stringResource(R.string.messages_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 15.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.textPrimary
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = themeColors.textPrimary,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }
        } else if (filteredConversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (searchQuery.isBlank()) {
                    EmptyStateView(
                        title = stringResource(R.string.empty_inbox_title),
                        subtitle = stringResource(R.string.empty_inbox_subtitle),
                        lottieRes = R.raw.anim_empty_chat,
                        fallbackIcon = Icons.Outlined.ChatBubbleOutline,
                        animationSize = 210.dp
                    )
                } else {
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conversation ->
                    MessageItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation.itemId, conversation.buyerId) }
                    )
                }
            }
        }
    }
}

/**
 * Message Row matching exact Figma CSS tokens.
 * Ellipse 13..20: 41x41 avatar, Name (Matter Medium 14sp), Snippet (Matter Regular 10sp), Time (Matter Regular 8sp).
 */
@Composable
fun MessageItem(
    conversation: ConversationItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    fontWeight = FontWeight.Medium,
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

            Text(
                text = conversation.lastMessage,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                letterSpacing = (-0.2).sp,
                color = themeColors.textSubtle,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Time (Matter Regular 8sp)
        Text(
            text = conversation.timeAgo,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 8.sp,
            letterSpacing = (-0.16).sp,
            color = themeColors.textSubtle
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MessagesScreenPreview() {
    UniSwapTheme {
        MessagesScreen()
    }
}
