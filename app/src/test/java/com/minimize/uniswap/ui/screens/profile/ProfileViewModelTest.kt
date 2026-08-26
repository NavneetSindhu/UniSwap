package com.minimize.uniswap.ui.screens.profile

import app.cash.turbine.test
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ItemRepository
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
class ProfileViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)

    private lateinit var viewModel: ProfileViewModel

    private val sampleUser = User(
        uid = "user_profile_1",
        email = "student@campus.edu",
        displayName = "Eco Champion",
        campusCenter = "North Campus",
        avatarId = "avatar_scholar",
        isEmailVerified = true
    )

    private val samplePreferences = UserPreferences(
        campusCenter = "North Campus"
    )

    private val sellerItems = listOf(
        CampusItem(
            id = "item_p1",
            title = "Chemistry Lab Manual",
            price = 0.0,
            status = ItemStatus.SOLD,
            sellerId = "user_profile_1",
            category = ItemCategory.ENGINEERING
        ),
        CampusItem(
            id = "item_p2",
            title = "Study Table",
            price = 1200.0,
            status = ItemStatus.AVAILABLE,
            sellerId = "user_profile_1",
            category = ItemCategory.DORM_ESSENTIALS
        )
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user_profile_1"
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)
        every { preferencesManager.preferencesFlow } returns flowOf(samplePreferences)
        every { itemRepository.getItemsBySellerFlow("user_profile_1") } returns flowOf(sellerItems)
        every { itemRepository.getSavedItemsFlow() } returns flowOf(emptyList())

        viewModel = ProfileViewModel(
            repository = itemRepository,
            authRepository = authRepository,
            preferencesManager = preferencesManager
        )
    }

    @Test
    fun observeUserProfile_updatesStateWithUserAndMetrics() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Eco Champion", state.userName)
            assertEquals("student@campus.edu", state.userEmail)
            assertTrue(state.isVerified)
            // 1 item rehomed (ItemStatus.SOLD)
            assertEquals(1, state.itemsRecycled)
            assertEquals(1.8, state.kgSaved, 0.01)
            assertEquals(3.2, state.co2Saved, 0.01)
            assertEquals(1, state.sellingItems.size)
        }
    }

    @Test
    fun updateAvatar_callsAuthRepository() = runTest {
        coEvery { authRepository.updateAvatar(any()) } returns Result.success(Unit)

        viewModel.updateAvatar("avatar_creative")

        coVerify { authRepository.updateAvatar("avatar_creative") }
    }

    @Test
    fun deleteItem_callsItemRepository() = runTest {
        coEvery { itemRepository.deleteItem(any()) } returns true

        viewModel.deleteItem("item_p2")

        coVerify { itemRepository.deleteItem("item_p2") }
    }
}
