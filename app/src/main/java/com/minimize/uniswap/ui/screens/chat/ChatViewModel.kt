package com.minimize.uniswap.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.Message
import com.minimize.uniswap.data.model.MessageStatus
import com.minimize.uniswap.data.model.Report
import com.minimize.uniswap.data.model.ReportReason
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    val currentUserId: String
        get() = authRepository.getCurrentUserId() ?: ""
    val isGuestMode: StateFlow<Boolean> = authRepository.isGuestMode
    private var activeBuyerId: String = ""

    private val _item = MutableStateFlow<CampusItem?>(null)
    val item: StateFlow<CampusItem?> = _item.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = combine(
        _isLoading,
        com.minimize.uniswap.util.DebugConfig.forceShimmerLoading
    ) { loading, forceShimmer ->
        loading || forceShimmer
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _isSubmittingReport = MutableStateFlow(false)
    val isSubmittingReport: StateFlow<Boolean> = _isSubmittingReport.asStateFlow()

    private val _isBlockingUser = MutableStateFlow(false)
    val isBlockingUser: StateFlow<Boolean> = _isBlockingUser.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val blockedUserIds: StateFlow<Set<String>> = reportRepository.getBlockedUserIdsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private var messageJob: Job? = null
    private var itemJob: Job? = null

    fun loadItem(itemId: String, explicitBuyerId: String? = null) {
        if (itemId.isBlank()) return

        itemJob?.cancel()

        itemJob = viewModelScope.launch {
            itemRepository.getItemByIdFlow(itemId)
                .filterNotNull()
                .collect { campusItem ->
                    _item.value = campusItem
                    val effectiveBuyerId = if (!explicitBuyerId.isNullOrBlank()) {
                        explicitBuyerId
                    } else if (currentUserId != campusItem.sellerId) {
                        currentUserId
                    } else {
                        currentUserId
                    }
                    activeBuyerId = effectiveBuyerId
                    observeMessages(campusItem.id, effectiveBuyerId, campusItem.sellerId)
                    markChatAsRead(campusItem.id, effectiveBuyerId, campusItem.sellerId)
                }
        }
    }

    private fun observeMessages(itemId: String, buyerId: String, sellerId: String) {
        messageJob?.cancel()
        messageJob = chatRepository.getMessages(itemId, buyerId, sellerId)
            .onEach { messageList ->
                _messages.value = messageList
                _isLoading.value = false
                // If there are incoming unread messages, mark them as read
                if (messageList.any { it.senderId != currentUserId && it.readAt == null }) {
                    markChatAsRead(itemId, buyerId, sellerId)
                }
            }
            .catch { e ->
                timber.log.Timber.e(e, "Error observing messages")
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun markChatAsRead(
        itemId: String = _item.value?.id ?: "",
        buyerId: String = if (activeBuyerId.isNotBlank()) activeBuyerId else currentUserId,
        sellerId: String = _item.value?.sellerId ?: ""
    ) {
        if (itemId.isBlank() || currentUserId.isBlank()) return
        viewModelScope.launch {
            chatRepository.markChatAsRead(
                itemId = itemId,
                buyerId = buyerId,
                sellerId = sellerId,
                currentUserId = currentUserId
            )
        }
    }

    fun sendMessage(text: String) {
        val currentItem = _item.value ?: return
        if (text.isBlank() || currentUserId.isBlank()) return

        val buyerId = if (activeBuyerId.isNotBlank()) activeBuyerId else currentUserId

        val newMessage = Message(
            senderId = currentUserId,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING // Optimistic state
        )

        val currentUser = authRepository.getCurrentUser()
        val currentUserName = currentUser?.displayName?.ifBlank { "User" } ?: "User"

        viewModelScope.launch {
            try {
                Timber.d("Sending message for item %s from %s", currentItem.id, currentUserId)
                chatRepository.sendMessage(
                    itemId = currentItem.id,
                    buyerId = buyerId,
                    sellerId = currentItem.sellerId,
                    message = newMessage,
                    itemTitle = currentItem.title,
                    itemImageUrl = currentItem.imageUrl,
                    buyerName = if (currentUserId == buyerId) currentUserName else "Buyer",
                    sellerName = if (currentUserId == currentItem.sellerId) currentUserName else currentItem.sellerName
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to send chat message in conversation: %s", currentItem.id)
            }
        }
    }

    fun editMessage(messageId: String, newText: String) {
        val currentItem = _item.value ?: return
        if (newText.isBlank() || messageId.isBlank()) return
        val buyerId = if (activeBuyerId.isNotBlank()) activeBuyerId else currentUserId

        viewModelScope.launch {
            chatRepository.editMessage(
                itemId = currentItem.id,
                buyerId = buyerId,
                sellerId = currentItem.sellerId,
                messageId = messageId,
                newText = newText
            )
        }
    }

    fun deleteMessage(messageId: String) {
        val currentItem = _item.value ?: return
        if (messageId.isBlank()) return
        val buyerId = if (activeBuyerId.isNotBlank()) activeBuyerId else currentUserId

        viewModelScope.launch {
            chatRepository.deleteMessage(
                itemId = currentItem.id,
                buyerId = buyerId,
                sellerId = currentItem.sellerId,
                messageId = messageId
            )
        }
    }

    fun clearChatHistory(onComplete: () -> Unit = {}) {
        val currentItem = _item.value ?: return
        val buyerId = if (activeBuyerId.isNotBlank()) activeBuyerId else currentUserId

        viewModelScope.launch {
            chatRepository.clearChatHistory(
                itemId = currentItem.id,
                buyerId = buyerId,
                sellerId = currentItem.sellerId,
                userId = currentUserId
            )
            onComplete()
        }
    }

    fun deleteConversation(onComplete: () -> Unit = {}) {
        val currentItem = _item.value ?: return
        val buyerId = if (activeBuyerId.isNotBlank()) activeBuyerId else currentUserId
        val uids = listOf(buyerId, currentItem.sellerId).sorted()
        val chatId = "${currentItem.id}_${uids[0]}_${uids[1]}"

        viewModelScope.launch {
            chatRepository.deleteConversation(chatId, currentUserId)
            onComplete()
        }
    }

    fun submitReport(reason: ReportReason, additionalDetails: String, onComplete: (Boolean) -> Unit) {
        val currentItem = _item.value ?: return
        val targetUserId = if (currentUserId == currentItem.sellerId) activeBuyerId else currentItem.sellerId

        viewModelScope.launch {
            _isSubmittingReport.value = true
            val report = Report(
                reportedUserId = targetUserId,
                itemId = currentItem.id,
                itemTitle = currentItem.title,
                reason = reason,
                additionalDetails = additionalDetails
            )
            val result = reportRepository.submitReport(report)
            _isSubmittingReport.value = false
            _userMessage.value = if (result.isSuccess) "Report submitted successfully" else "Failed to submit report"
            onComplete(result.isSuccess)
        }
    }

    fun blockOtherUser(onComplete: (Boolean) -> Unit) {
        val currentItem = _item.value ?: return
        val targetUserId = if (currentUserId == currentItem.sellerId) activeBuyerId else currentItem.sellerId

        viewModelScope.launch {
            _isBlockingUser.value = true
            val result = reportRepository.blockUser(targetUserId)
            _isBlockingUser.value = false
            _userMessage.value = if (result.isSuccess) "User blocked" else "Failed to block user"
            onComplete(result.isSuccess)
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
