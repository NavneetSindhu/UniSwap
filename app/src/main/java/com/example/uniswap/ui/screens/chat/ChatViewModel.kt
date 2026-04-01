package com.example.uniswap.ui.screens.chat

import androidx.lifecycle.ViewModel
import com.example.uniswap.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel : ViewModel() {
    private val myUserId = "me_123"

    private val _messages = MutableStateFlow(listOf(
        Message(senderId = "other", text = "Hey! Is the lamp still available?", timestamp = "10:42 AM"),
        Message(senderId = "other", text = "I can meet at the Library Foyer.", timestamp = "10:43 AM")
    ))
    val messages = _messages.asStateFlow()

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