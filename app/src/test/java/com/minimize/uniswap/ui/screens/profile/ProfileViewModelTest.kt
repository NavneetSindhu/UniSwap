package com.minimize.uniswap.ui.screens.profile

import app.cash.turbine.test
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.model.UserProfile
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

    private val sampleUser = UserProfile(
        uid = "user_456",
        email = "profile_user@campus.edu",
        displayName = "Eco Champion",
        campusCenter = "Main Campus",
        avatarId = "avatar_scholar",
        isEmailVerified = true
    )

    private val sampleItems = listOf(
        CampusItem(
            id = "item_1",
            title = "Dorm Lamp",
            price = 200.0,
            sellerId = "user_456",
            status = ItemStatus.AVAILABLE,
            category = ItemCategory.DORM_ESSENTIALS
        ),
        CampusItem(
            id = "item_2",
            title = "Chemistry Notes",
            price = 0.0,
            sellerId = "user_456",
            status = ItemStatus.SOLD,
            category = ItemCategory.BOOKS
        ),
        CampusItem(
            id = "item_3",
            title = "Lab Coat",
            price = 150.0,
            sellerId = "user_456",
            status = ItemStatus.SOLD,
            category = ItemCategory.OTHER
        )
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user_456"
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)
        every { preferencesManager.preferencesFlow } returns flowOf(UserPreferences(campusCenter = "Main Campus"))
        every { itemRepository.getItemsBySellerFlow("user_456") } returns flowOf(sampleItems)
        every { itemRepository.getSavedItemsFlow() } returns flowOf(listOf(sampleItems[0]))

        viewModel = ProfileViewModel(
            repository = itemRepository,
            authRepository = authRepository,
            preferencesManager = preferencesManager
        )
    }

    @Test
    fun userProfile_isCorrectlyAggregatedIntoUiState() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Eco Champion", state.userName)
            assertEquals("profile_user@campus.edu", state.userEmail)
            assertEquals("Main Campus", state.campusCenter)
            assertTrue(state.isVerified)
            assertEquals(1, state.sellingItems.size)
            assertEquals(1, state.givenAwayItems.size)
            assertEquals(2, state.itemsRecycled) // 2 sold items
            assertEquals(3.6, state.kgSaved, 0.01) // 2 * 1.8 kg
            assertEquals(6.4, state.co2Saved, 0.01) // 2 * 3.2 kg
        }
    }

    @Test
    fun updateAvatar_callsAuthRepository() = runTest {
        coEvery { authRepository.updateAvatar("avatar_creator") } returns Result.success(Unit)

        viewModel.updateAvatar("avatar_creator")

        coVerify { authRepository.updateAvatar("avatar_creator") }
    }

    @Test
    fun deleteItem_callsRepository() = runTest {
        coEvery { itemRepository.deleteItem("item_1") } returns Result.success(Unit)

        viewModel.deleteItem("item_1")

        coVerify { itemRepository.deleteItem("item_1") }
    }

    @Test
    fun removeSavedItem_togglesSaveInRepository() = runTest {
        viewModel.removeSavedItem("item_1")

        coVerify { itemRepository.toggleSaveItem("item_1") }
    }
}
