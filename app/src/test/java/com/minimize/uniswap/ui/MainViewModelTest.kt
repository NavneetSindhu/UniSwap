package com.minimize.uniswap.ui

import android.content.Context
import app.cash.turbine.test
import com.minimize.uniswap.data.model.ChatThread
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.util.MainCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var viewModel: MainViewModel

    private val sampleUser = User(uid = "user_main_1", displayName = "Main User")
    private val samplePrefs = UserPreferences(campusCenter = "Main Campus")

    private val unreadThread = ChatThread(
        id = "thread_1",
        itemId = "item_1",
        sellerId = "user_main_1",
        buyerId = "buyer_9",
        lastSenderId = "buyer_9",
        lastMessage = "Is it available?",
        unreadByParticipantIds = listOf("user_main_1")
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user_main_1"
        every { authRepository.isGuestMode } returns MutableStateFlow(false)
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)
        every { preferencesManager.preferencesFlow } returns flowOf(samplePrefs)
        every { chatRepository.getChatThreadsFlow("user_main_1") } returns flowOf(listOf(unreadThread))

        viewModel = MainViewModel(
            authRepository = authRepository,
            chatRepository = chatRepository,
            preferencesManager = preferencesManager,
            context = context
        )
    }

    @Test
    fun userPreferences_isLoaded() = runTest {
        viewModel.userPreferences.test {
            val prefs = awaitItem()
            assertNotNull(prefs)
            assertEquals("Main Campus", prefs?.campusCenter)
        }
    }

    @Test
    fun hasUnreadMessages_isTrue_whenThreadHasUnreadByCurrentUser() = runTest {
        viewModel.hasUnreadMessages.test {
            val hasUnread = awaitItem()
            assertTrue(hasUnread)
        }
    }
}
