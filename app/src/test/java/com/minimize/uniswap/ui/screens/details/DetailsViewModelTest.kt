package com.minimize.uniswap.ui.screens.details

import app.cash.turbine.test
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.model.UserProfile
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.prompt.GlobalPromptManager
import com.minimize.uniswap.data.prompt.PromptType
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ItemRepository
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
class DetailsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val reportRepository: ReportRepository = mockk(relaxed = true)
    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)
    private val promptManager: GlobalPromptManager = mockk(relaxed = true)

    private lateinit var viewModel: DetailsViewModel

    private val sampleUser = UserProfile(
        uid = "test_user_123",
        email = "student@campus.edu",
        displayName = "Navneet",
        isEmailVerified = false
    )

    private val sampleItem = CampusItem(
        id = "item_123",
        title = "Calculus Textbook",
        description = "Early Transcendentals 8th Edition",
        price = 450.0,
        sellerId = "seller_456",
        sellerName = "Senior Student",
        category = ItemCategory.BOOKS,
        status = ItemStatus.AVAILABLE
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns sampleUser.uid
        every { authRepository.isGuestMode } returns flowOf(false)
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)

        viewModel = DetailsViewModel(
            repository = itemRepository,
            authRepository = authRepository,
            reportRepository = reportRepository,
            preferencesManager = preferencesManager,
            promptManager = promptManager
        )
    }

    @Test
    fun initialUserData_isObservedCorrectly() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("test_user_123", state.currentUserId)
            assertEquals("student@campus.edu", state.userEmail)
            assertFalse(state.isEmailVerified)
            assertFalse(state.isGuestMode)
        }
    }

    @Test
    fun getItem_loadsItemSuccessfully() = runTest {
        every { itemRepository.getItemFlow("item_123") } returns flowOf(sampleItem)
        every { itemRepository.isItemSavedFlow("item_123") } returns flowOf(false)

        viewModel.getItem("item_123")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(sampleItem, state.item)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun getItem_withBlankId_setsError() = runTest {
        viewModel.getItem("   ")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Invalid item ID.", state.error)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun sendVerificationEmail_success_updatesStateAndPreferences() = runTest {
        coEvery { authRepository.sendVerificationEmail() } returns Result.success(Unit)

        viewModel.sendVerificationEmail(email = "student@campus.edu", studentId = "2023CS01")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isVerificationSent)
            assertFalse(state.isProcessingVerification)
        }

        coVerify {
            preferencesManager.updateStudentVerificationDetails(
                collegeEmail = "student@campus.edu",
                studentId = "2023CS01",
                isPending = true,
                sentTimestamp = any()
            )
        }
    }

    @Test
    fun checkVerificationStatus_callsReloadUser() = runTest {
        coEvery { authRepository.reloadUser() } returns Result.success(Unit)

        viewModel.checkVerificationStatus()

        coVerify { authRepository.reloadUser() }
    }

    @Test
    fun dismissNudge_recordsPromptShownInPromptManager() = runTest {
        viewModel.dismissNudge()

        coVerify {
            promptManager.recordPromptShown(PromptType.STUDENT_VERIFICATION)
        }

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.showNudge)
            assertFalse(state.showVerificationFlow)
        }
    }
}
