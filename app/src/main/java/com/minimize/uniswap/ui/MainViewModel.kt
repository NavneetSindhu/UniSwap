package com.minimize.uniswap.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.util.LocalNotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val preferencesManager: UserPreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /**
     * Exposes user preferences to the UI.
     * We use stateIn with SharingStarted.Eagerly to ensure it's loaded as soon as the VM is created.
     */
    val userPreferences: StateFlow<UserPreferences?> = preferencesManager.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null // null indicates "not loaded yet"
        )

    private val _hasUnreadMessages = MutableStateFlow(false)
    val hasUnreadMessages: StateFlow<Boolean> = _hasUnreadMessages.asStateFlow()

    private val notifiedMessageTimestamps = mutableSetOf<Long>()
    private val sessionStartTime = System.currentTimeMillis()

    init {
        observeIncomingMessages()
    }

    private fun observeIncomingMessages() {
        viewModelScope.launch {
            authRepository.getUserFlow()
                .filterNotNull()
                .flatMapLatest { user ->
                    chatRepository.getChatThreadsFlow(user.uid)
                }
                .collect { threads ->
                    val myUid = authRepository.getCurrentUserId() ?: ""

                    // Unread badge logic: true only if thread has unread messages for current user
                    val hasUnread = threads.any { thread ->
                        thread.unreadByParticipantIds.contains(myUid)
                    }
                    _hasUnreadMessages.value = hasUnread

                    // Trigger local notifications for new incoming messages / chats
                    threads.forEach { thread ->
                        if (thread.lastSenderId.isNotBlank() &&
                            thread.lastSenderId != myUid &&
                            thread.lastMessageTimestamp > sessionStartTime &&
                            !notifiedMessageTimestamps.contains(thread.lastMessageTimestamp)
                        ) {
                            notifiedMessageTimestamps.add(thread.lastMessageTimestamp)
                            val senderName = if (myUid == thread.sellerId) thread.buyerName else thread.sellerName
                            val title = if (senderName.isNotBlank() && senderName != "User") {
                                "$senderName (${thread.itemTitle})"
                            } else {
                                "New message on ${thread.itemTitle}"
                            }

                            LocalNotificationHelper.showChatNotification(
                                context = context,
                                notificationId = (thread.lastMessageTimestamp % Int.MAX_VALUE).toInt(),
                                title = title,
                                message = thread.lastMessage,
                                itemId = thread.itemId,
                                buyerId = thread.buyerId
                            )
                        }
                    }
                }
        }
    }

    fun markMessagesAsRead() {
        _hasUnreadMessages.value = false
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
