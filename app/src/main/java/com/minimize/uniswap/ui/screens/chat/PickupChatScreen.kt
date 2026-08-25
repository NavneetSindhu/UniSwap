package com.minimize.uniswap.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.MessageStatus
import com.minimize.uniswap.ui.components.*
import com.minimize.uniswap.ui.theme.*

data class ChatBubbleMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT
)

/**
 * 1-on-1 Chat Interface matching exact Figma CSS tokens.
 * Features 50dp bottom-rounded header, 29x29 avatar, verified student badge,
 * message edit/delete actions, clear history, and 50dp capsule input bar.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    var editingMessage by remember { mutableStateOf<ChatBubbleMessage?>(null) }
    val listState = rememberLazyListState()

    var isActionSheetOpen by remember { mutableStateOf(false) }
    var isReportSheetOpen by remember { mutableStateOf(false) }
    var isBlockDialogOpen by remember { mutableStateOf(false) }

    var selectedMessageForAction by remember { mutableStateOf<ChatBubbleMessage?>(null) }
    var messageToDelete by remember { mutableStateOf<ChatBubbleMessage?>(null) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showDeleteConversationDialog by remember { mutableStateOf(false) }

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
    val partnerAvatarId = if (isSeller) "avatar_scholar" else (item?.sellerAvatarId ?: "avatar_scholar")

    val chatMessages = remember(liveMessages) {
        liveMessages.map {
            ChatBubbleMessage(
                id = it.id,
                text = it.text,
                isFromMe = it.senderId == viewModel.currentUserId,
                isEdited = it.isEdited,
                isDeleted = it.isDeleted,
                status = it.status
            )
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val themeColors = UniSwapTheme.colors

    // Safety & Management Action Bottom Sheet (from top bar 3-dots)
    if (isActionSheetOpen && item != null) {
        AppBottomSheet(
            onDismissRequest = { isActionSheetOpen = false },
            heightFraction = null,
            containerColor = themeColors.cardBackground,
            contentColor = themeColors.textPrimary,
            showCloseIcon = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item!!.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Chat with $studentName",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = themeColors.textSecondary
                    )
                )

                HorizontalDivider(
                    color = themeColors.divider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Clear Chat History
                ActionSheetRow(
                    icon = Icons.Outlined.CleaningServices,
                    title = stringResource(R.string.action_clear_chat),
                    onClick = {
                        isActionSheetOpen = false
                        showClearChatDialog = true
                    }
                )

                // Delete Conversation
                ActionSheetRow(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(R.string.action_delete_conversation),
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        isActionSheetOpen = false
                        showDeleteConversationDialog = true
                    }
                )

                // Report User
                ActionSheetRow(
                    icon = Icons.Outlined.Forum,
                    title = stringResource(R.string.action_report_listing),
                    onClick = {
                        isActionSheetOpen = false
                        isReportSheetOpen = true
                    }
                )

                // Block User
                ActionSheetRow(
                    icon = Icons.Default.Close,
                    title = stringResource(R.string.action_block_seller),
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        isActionSheetOpen = false
                        isBlockDialogOpen = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Message Bubble Action Bottom Sheet (from message long-press)
    selectedMessageForAction?.let { msg ->
        MessageActionBottomSheet(
            onDismissRequest = { selectedMessageForAction = null },
            messageText = msg.text,
            isFromMe = msg.isFromMe,
            isDeleted = msg.isDeleted,
            onCopyClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("chat_message", msg.text))
                Toast.makeText(context, context.getString(R.string.toast_text_copied), Toast.LENGTH_SHORT).show()
                selectedMessageForAction = null
            },
            onEditClick = {
                editingMessage = msg
                inputText = msg.text
                selectedMessageForAction = null
            },
            onDeleteClick = {
                val target = msg
                selectedMessageForAction = null
                messageToDelete = target
            }
        )
    }

    // Delete Single Message Confirmation Dialog
    messageToDelete?.let { msg ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_message_confirm_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_message_confirm_msg),
                    fontFamily = MatterFontFamily,
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val msgId = msg.id
                        messageToDelete = null
                        viewModel.deleteMessage(msgId)
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
                TextButton(onClick = { messageToDelete = null }) {
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

    // Clear Chat History Confirmation Dialog
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.clear_chat_confirm_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_chat_confirm_msg),
                    fontFamily = MatterFontFamily,
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearChatDialog = false
                        viewModel.clearChatHistory()
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
                TextButton(onClick = { showClearChatDialog = false }) {
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

    // Delete Conversation Confirmation Dialog
    if (showDeleteConversationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConversationDialog = false },
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
                        showDeleteConversationDialog = false
                        viewModel.deleteConversation {
                            onBackClick()
                        }
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
                TextButton(onClick = { showDeleteConversationDialog = false }) {
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
                            UserAvatar(
                                avatarId = partnerAvatarId,
                                modifier = Modifier.fillMaxSize()
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

                            Icon(
                                painter = painterResource(id = R.drawable.ic_verified),
                                contentDescription = "Verified Student",
                                tint = Color.Unspecified,
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
            Surface(
                color = themeColors.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.union(WindowInsets.ime)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    // Editing Banner (if in edit mode)
                    AnimatedVisibility(
                        visible = editingMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp, start = 8.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.editing_message_label),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = themeColors.wasteMetricGreen
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Edit",
                                tint = themeColors.textSubtle,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        editingMessage = null
                                        inputText = ""
                                    }
                            )
                        }
                    }

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
                                                    val currentEdit = editingMessage
                                                    if (currentEdit != null) {
                                                        viewModel.editMessage(currentEdit.id, inputText)
                                                        editingMessage = null
                                                    } else {
                                                        viewModel.sendMessage(inputText)
                                                    }
                                                    inputText = ""
                                                }
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Send / Update Button
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (inputText.isNotBlank()) themeColors.textPrimary else themeColors.btnBackBg.copy(alpha = 0.5f))
                                        .clickable(enabled = inputText.isNotBlank()) {
                                            val currentEdit = editingMessage
                                            if (currentEdit != null) {
                                                viewModel.editMessage(currentEdit.id, inputText)
                                                editingMessage = null
                                            } else {
                                                viewModel.sendMessage(inputText)
                                            }
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
                    ChatBubbleRow(
                        message = message,
                        partnerAvatarId = partnerAvatarId,
                        onLongClick = { selectedMessageForAction = message }
                    )
                }
            }
        }
    }
}

/**
 * Message Row matching Figma specs with Long-press action support.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubbleRow(
    message: ChatBubbleMessage,
    partnerAvatarId: String? = null,
    onLongClick: () -> Unit = {}
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
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = if (message.isDeleted) stringResource(R.string.message_deleted_placeholder) else message.text,
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        letterSpacing = (-0.2).sp,
                        fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal,
                        color = if (message.isDeleted) Color.White.copy(alpha = 0.5f) else Color.White
                    )
                    
                    // Bottom status row (Edited tag + Read Receipt ticks)
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.isEdited && !message.isDeleted) {
                            Text(
                                text = stringResource(R.string.message_edited_tag),
                                fontFamily = MatterFontFamily,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        if (!message.isDeleted) {
                            MessageReceiptStatus(
                                status = message.status,
                                size = 12.dp
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Incoming message (Left-aligned with 23x23 avatar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(23.dp)
                    .clip(CircleShape)
                    .background(themeColors.btnBackBg)
            ) {
                UserAvatar(
                    avatarId = partnerAvatarId,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(themeColors.cardSurface)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = if (message.isDeleted) stringResource(R.string.message_deleted_placeholder) else message.text,
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        letterSpacing = (-0.2).sp,
                        fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal,
                        color = if (message.isDeleted) themeColors.textSubtle else themeColors.textPrimary
                    )
                    if (message.isEdited && !message.isDeleted) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.message_edited_tag),
                            fontFamily = MatterFontFamily,
                            fontSize = 9.sp,
                            color = themeColors.textSubtle,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
