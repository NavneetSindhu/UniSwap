package com.minimize.uniswap.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.Message
import com.minimize.uniswap.data.model.MessageStatus
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUserId: String = authRepository.getCurrentUserId() ?: ""
    private var activeBuyerId: String = ""

    private val _item = MutableStateFlow<CampusItem?>(null)
    val item: StateFlow<CampusItem?> = _item.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

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
                }
        }
    }

    private fun observeMessages(itemId: String, buyerId: String, sellerId: String) {
        messageJob?.cancel()
        messageJob = chatRepository.getMessages(itemId, buyerId, sellerId)
            .onEach { _messages.value = it }
            .launchIn(viewModelScope)
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
        }
    }
}
