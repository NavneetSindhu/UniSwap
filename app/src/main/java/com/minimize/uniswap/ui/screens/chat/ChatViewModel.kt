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

    private val _item = MutableStateFlow<CampusItem?>(null)
    val item: StateFlow<CampusItem?> = _item.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var messageJob: Job? = null
    private var itemJob: Job? = null

    fun loadItem(itemId: String) {
        if (itemId.isBlank()) return

        itemJob?.cancel()

        itemJob = viewModelScope.launch {
            itemRepository.getItemByIdFlow(itemId)
                .filterNotNull()
                .collect { campusItem ->
                    _item.value = campusItem
                    // In a 1-to-1 chat for a specific item, the participants are the buyer and the seller.
                    // If current user is seller, they are chatting with the person who initiated (buyerId).
                    // For simplicity in this v1, we assume currentUserId is the one initiating if they aren't the seller.
                    val buyerId = currentUserId 
                    observeMessages(campusItem.id, buyerId, campusItem.sellerId)
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

        val buyerId = currentUserId // Consistent with observeMessages

        val newMessage = Message(
            senderId = currentUserId,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING // Optimistic state
        )

        viewModelScope.launch {
            chatRepository.sendMessage(
                itemId = currentItem.id,
                buyerId = buyerId,
                sellerId = currentItem.sellerId,
                message = newMessage
            )
        }
    }
}
