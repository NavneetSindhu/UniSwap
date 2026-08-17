package com.minimize.uniswap.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.Message
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val myUserId = "me_123"

    // State for the specific item being discussed
    private val _item = MutableStateFlow<CampusItem?>(null)
    val item = _item.asStateFlow()

    // Real-time messages from Firestore
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private var messageJob: kotlinx.coroutines.Job? = null

    /**
     * Fetches the specific item details and starts observing messages.
     */
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            try {
                val allItems = itemRepository.getItems()
                val foundItem = allItems.find { it.id == itemId }
                _item.value = foundItem

                foundItem?.let {
                    observeMessages(it.id, it.sellerId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _item.value = null
            }
        }
    }

    private fun observeMessages(itemId: String, sellerId: String) {
        messageJob?.cancel()
        messageJob = chatRepository.getMessages(itemId, myUserId, sellerId)
            .onEach { _messages.value = it }
            .launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        val currentItem = _item.value ?: return
        if (text.isBlank()) return

        val newMessage = Message(
            senderId = myUserId,
            text = text,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )

        viewModelScope.launch {
            chatRepository.sendMessage(currentItem.id, myUserId, currentItem.sellerId, newMessage)
        }
    }
}
