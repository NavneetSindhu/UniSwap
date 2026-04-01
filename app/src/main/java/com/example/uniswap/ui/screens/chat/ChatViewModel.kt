package com.example.uniswap.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.CampusItem
import com.example.uniswap.data.model.Message
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.NetworkItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(
    // Default to the network repository to fetch real item data
    private val repository: ItemRepository = NetworkItemRepository()
) : ViewModel() {

    private val myUserId = "me_123"

    // State for the specific item being discussed
    private val _item = MutableStateFlow<CampusItem?>(null)
    val item = _item.asStateFlow()

    // Mock initial messages
    private val _messages = MutableStateFlow(listOf(
        Message(senderId = "other", text = "Hey! Is this still available?", timestamp = "10:42 AM"),
        Message(senderId = "other", text = "I can meet at the Library Foyer.", timestamp = "10:43 AM")
    ))
    val messages = _messages.asStateFlow()

    /**
     * Fetches the specific item details from the backend using the ID.
     */
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            try {
                val allItems = repository.getItems()
                // Find the item that matches the ID passed from the navigation
                val foundItem = allItems.find { it.id == itemId }
                _item.value = foundItem
            } catch (e: Exception) {
                e.printStackTrace()
                _item.value = null
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val newMessage = Message(
            senderId = myUserId,
            text = text,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )

        _messages.update { it + newMessage }
    }
}