package com.minimize.uniswap.ui.screens.messages

import app.cash.turbine.test
import com.minimize.uniswap.data.model.ChatThread
import com.minimize.uniswap.data.model.MessageStatus
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.data.repository.ReportRepository
import com.minimize.uniswap.util.MainCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val reportRepository: ReportRepository = mockk(relaxed = true)

    private lateinit var viewModel: MessagesViewModel

    private val sampleThread = ChatThread(
        id = "thread_123",
        itemId = "item_456",
        itemTitle = "Lab Coat",
        buyerId = "user_me",
        buyerName = "Me",
        sellerId = "user_other",
        sellerName = "Senior Student",
        lastMessage = "Can we meet tomorrow?",
        lastSenderId = "user_other",
        lastMessageTimestamp = System.currentTimeMillis(),
        lastMessageStatus = MessageStatus.DELIVERED,
        unreadByParticipantIds = listOf("user_me")
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user_me"
        every { authRepository.getUserFlow() } returns flowOf(User(uid = "user_me", displayName = "Me"))
        every { chatRepository.getChatThreadsFlow("user_me") } returns flowOf(listOf(sampleThread))

        viewModel = MessagesViewModel(
            chatRepository = chatRepository,
            authRepository = authRepository,
            reportRepository = reportRepository
        )
    }

    @Test
    fun loadThreads_mapsChatThreadsIntoUiModels() = runTest {
        viewModel.threads.test {
            val threadList = awaitItem()
            assertEquals(1, threadList.size)
            val uiModel = threadList.first()
            assertEquals("thread_123", uiModel.id)
            assertEquals("Senior Student", uiModel.displayName)
            assertEquals("Can we meet tomorrow?", uiModel.lastMessage)
            assertTrue(uiModel.isUnread)
            assertFalse(uiModel.isLastMessageFromMe)
        }
    }

    @Test
    fun deleteConversation_callsChatRepository() = runTest {
        coEvery { chatRepository.deleteConversation("thread_123", "user_me") } returns Result.success(Unit)
        var callbackSuccess = false

        viewModel.deleteConversation("thread_123") { success ->
            callbackSuccess = success
        }

        coVerify { chatRepository.deleteConversation("thread_123", "user_me") }
        assertTrue(callbackSuccess)
    }

    @Test
    fun blockUser_callsReportRepository() = runTest {
        coEvery { reportRepository.blockUser("user_other") } returns Result.success(Unit)
        var callbackExecuted = false

        viewModel.blockUser("user_other") {
            callbackExecuted = true
        }

        coVerify { reportRepository.blockUser("user_other") }
        assertTrue(callbackExecuted)
    }
}
