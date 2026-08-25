package com.minimize.uniswap.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.ChatThread
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.minimize.uniswap.data.model.MessageStatus
import com.minimize.uniswap.data.repository.ChatRepository

data class ConversationItemUiModel(
    val id: String,
    val itemId: String,
    val buyerId: String,
    val sellerId: String,
    val displayName: String,
    val lastMessage: String,
    val timeAgo: String,
    val avatarUrl: String? = null,
    val itemTitle: String = "",
    val isUnread: Boolean = false,
    val isLastMessageFromMe: Boolean = false,
    val lastMessageStatus: MessageStatus = MessageStatus.SENT
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    val currentUserId: String = authRepository.getCurrentUserId() ?: ""
    val currentUserFlow: Flow<com.minimize.uniswap.data.model.User?> = authRepository.getUserFlow()

    private val _threads = MutableStateFlow<List<ConversationItemUiModel>>(emptyList())
    val threads: StateFlow<List<ConversationItemUiModel>> = _threads.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = combine(
        _isLoading,
        com.minimize.uniswap.util.DebugConfig.forceShimmerLoading
    ) { loading, forceShimmer ->
        loading || forceShimmer
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun deleteConversation(threadId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = chatRepository.deleteConversation(threadId, currentUserId)
            onComplete(result.isSuccess)
        }
    }

    fun blockUser(otherUserId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            reportRepository.blockUser(otherUserId)
            onComplete()
        }
    }

    init {
        loadThreads()
    }

    private fun loadThreads() {
        if (currentUserId.isBlank()) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            chatRepository.getChatThreadsFlow(currentUserId)
                .map { chatThreads ->
                    chatThreads.map { thread ->
                        val isSeller = currentUserId == thread.sellerId
                        val otherUserName = if (isSeller) thread.buyerName.ifBlank { "Buyer" } else thread.sellerName.ifBlank { "Seller" }
                        val formattedTime = formatTimestamp(thread.lastMessageTimestamp)
                        val isUnread = thread.unreadByParticipantIds.contains(currentUserId)
                        val isLastFromMe = thread.lastSenderId == currentUserId

                        ConversationItemUiModel(
                            id = thread.id,
                            itemId = thread.itemId,
                            buyerId = thread.buyerId,
                            sellerId = thread.sellerId,
                            displayName = otherUserName,
                            lastMessage = thread.lastMessage,
                            timeAgo = formattedTime,
                            avatarUrl = thread.itemImageUrl.ifBlank { null },
                            itemTitle = thread.itemTitle,
                            isUnread = isUnread,
                            isLastMessageFromMe = isLastFromMe,
                            lastMessageStatus = thread.lastMessageStatus
                        )
                    }
                }
                .collect { uiModels ->
                    _threads.value = uiModels
                    _isLoading.value = false
                }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return "Just now"
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> "${days / 7}w ago"
        }
    }
}
