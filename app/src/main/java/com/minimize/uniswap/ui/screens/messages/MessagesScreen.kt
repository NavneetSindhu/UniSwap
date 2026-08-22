package com.minimize.uniswap.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.*

data class Conversation(
    val id: String,
    val senderName: String,
    val lastMessage: String,
    val time: String,
    val avatarUrl: String? = null
)

/**
 * Messages Screen displaying all active chats.
 * Features 42x42 profile avatar, 50dp capsule search with 30x30 white circle,
 * 14sp "Messages" section heading, and chat rows with 41x41 avatars.
 */
@Composable
fun MessagesScreen(
    onConversationClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val conversations = listOf(
        Conversation("1", stringResource(R.string.sender_lokesh), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago)),
        Conversation("2", stringResource(R.string.sender_navneet), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago)),
        Conversation("3", stringResource(R.string.sender_sakshi), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago)),
        Conversation("4", stringResource(R.string.sender_mohit), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago)),
        Conversation("5", stringResource(R.string.sender_n_chaudhary), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago)),
        Conversation("6", stringResource(R.string.sender_kamini), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago)),
        Conversation("7", stringResource(R.string.sender_ajay), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago)),
        Conversation("8", stringResource(R.string.sender_yuvraj), stringResource(R.string.sample_message_snippet), stringResource(R.string.sample_time_ago))
    )

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.senderName.contains(searchQuery, ignoreCase = true) ||
                    it.lastMessage.contains(searchQuery, ignoreCase = true)
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
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200",
                            contentDescription = stringResource(R.string.field_title_label),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
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
                                        text = stringResource(R.string.search_placeholder),
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
                    onClick = { onConversationClick(conversation.id) }
                )
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
    conversation: Conversation,
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
            if (!conversation.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = conversation.avatarUrl,
                    contentDescription = conversation.senderName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Center Content: Sender Name + Last Message Snippet
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = conversation.senderName,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 15.sp,
                letterSpacing = (-0.28).sp,
                color = themeColors.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = conversation.lastMessage,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                letterSpacing = (-0.2).sp,
                color = themeColors.textSubtle
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Time (Matter Regular 8sp)
        Text(
            text = conversation.time,
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
