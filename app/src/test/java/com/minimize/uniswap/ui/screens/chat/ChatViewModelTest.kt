package com.minimize.uniswap.ui.screens.chat

import app.cash.turbine.test
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.Message
import com.minimize.uniswap.data.model.UserProfile
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ChatRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.data.repository.ReportRepository
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
class ChatViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk(relaxed = true)
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val reportRepository: ReportRepository = mockk(relaxed = true)

    private lateinit var viewModel: ChatViewModel

    private val sampleItem = CampusItem(
        id = "item_chat_1",
        title = "Scientific Calculator",
        price = 600.0,
        sellerId = "seller_999",
        sellerName = "Seller Sarah",
        category = ItemCategory.ELECTRONICS
    )

    private val sampleMessages = listOf(
        Message(id = "msg_1", senderId = "buyer_123", text = "Hi, is this available?", timestamp = 1000L),
        Message(id = "msg_2", senderId = "seller_999", text = "Yes, can meet at Library.", timestamp = 2000L)
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "buyer_123"
        every { authRepository.isGuestMode } returns MutableStateFlow(false)
        every { authRepository.getCurrentUser() } returns UserProfile(uid = "buyer_123", displayName = "Buyer Bob")
        every { reportRepository.getBlockedUserIdsFlow() } returns MutableStateFlow(emptySet())
        every { itemRepository.getItemByIdFlow("item_chat_1") } returns flowOf(sampleItem)
        every { chatRepository.getMessages("item_chat_1", "buyer_123", "seller_999") } returns flowOf(sampleMessages)

        viewModel = ChatViewModel(
            itemRepository = itemRepository,
            chatRepository = chatRepository,
            authRepository = authRepository,
            reportRepository = reportRepository
        )
    }

    @Test
    fun loadItem_observesItemAndLoadsMessages() = runTest {
        viewModel.loadItem("item_chat_1", "buyer_123")

        viewModel.item.test {
            val item = awaitItem()
            assertEquals("item_chat_1", item?.id)
            assertEquals("Scientific Calculator", item?.title)
        }

        viewModel.messages.test {
            val msgs = awaitItem()
            assertEquals(2, msgs.size)
            assertEquals("Hi, is this available?", msgs[0].text)
        }
    }

    @Test
    fun sendMessage_callsChatRepository() = runTest {
        viewModel.loadItem("item_chat_1", "buyer_123")

        viewModel.sendMessage("Sounds great, see you at 4pm!")

        coVerify {
            chatRepository.sendMessage(
                itemId = "item_chat_1",
                buyerId = "buyer_123",
                sellerId = "seller_999",
                message = match { it.text == "Sounds great, see you at 4pm!" },
                itemTitle = "Scientific Calculator",
                itemImageUrl = any(),
                buyerName = any(),
                sellerName = any()
            )
        }
    }

    @Test
    fun editMessage_callsRepository() = runTest {
        viewModel.loadItem("item_chat_1", "buyer_123")

        viewModel.editMessage("msg_1", "Updated inquiry text")

        coVerify {
            chatRepository.editMessage(
                itemId = "item_chat_1",
                buyerId = "buyer_123",
                sellerId = "seller_999",
                messageId = "msg_1",
                newText = "Updated inquiry text"
            )
        }
    }

    @Test
    fun deleteMessage_callsRepository() = runTest {
        viewModel.loadItem("item_chat_1", "buyer_123")

        viewModel.deleteMessage("msg_1")

        coVerify {
            chatRepository.deleteMessage(
                itemId = "item_chat_1",
                buyerId = "buyer_123",
                sellerId = "seller_999",
                messageId = "msg_1"
            )
        }
    }
}
