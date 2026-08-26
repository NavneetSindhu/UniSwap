package com.minimize.uniswap.ui.screens.details

import app.cash.turbine.test
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.prompt.GlobalPromptManager
import com.minimize.uniswap.data.prompt.PromptType
import com.minimize.uniswap.data.repository.AuthRepository
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
class DetailsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val reportRepository: ReportRepository = mockk(relaxed = true)
    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)
    private val promptManager: GlobalPromptManager = mockk(relaxed = true)

    private lateinit var viewModel: DetailsViewModel

    private val sampleUser = User(
        uid = "user_details_1",
        email = "student@campus.edu",
        displayName = "John Doe",
        isEmailVerified = false
    )

    private val sampleItem = CampusItem(
        id = "item_details_1",
        title = "Calculus Textbook",
        price = 450.0,
        sellerId = "seller_456",
        sellerName = "Alice Senior",
        category = ItemCategory.ENGINEERING
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user_details_1"
        every { authRepository.isGuestMode } returns MutableStateFlow(false)
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)
        every { itemRepository.getItemByIdFlow("item_details_1") } returns flowOf(sampleItem)
        every { reportRepository.getBlockedUserIdsFlow() } returns MutableStateFlow(emptySet())
        every { itemRepository.getSavedItemIdsFlow() } returns flowOf(emptySet())

        viewModel = DetailsViewModel(
            repository = itemRepository,
            authRepository = authRepository,
            reportRepository = reportRepository,
            preferencesManager = preferencesManager,
            promptManager = promptManager
        )
    }

    @Test
    fun getItem_withValidId_updatesUiStateWithItem() = runTest {
        viewModel.getItem("item_details_1")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("item_details_1", state.item?.id)
            assertEquals("Calculus Textbook", state.item?.title)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun getItem_withBlankId_setsError() = runTest {
        viewModel.getItem("")

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.item)
            assertEquals("Invalid item ID.", state.error)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun sendVerificationEmail_callsRepositoryAndPreferences() = runTest {
        coEvery { authRepository.sendVerificationEmail() } returns Result.success(Unit)

        viewModel.sendVerificationEmail(email = "student@campus.edu", studentId = "2023BCSE01")

        coVerify { authRepository.sendVerificationEmail() }
        coVerify {
            preferencesManager.updateStudentVerificationDetails(
                collegeEmail = "student@campus.edu",
                studentId = "2023BCSE01",
                isPending = true,
                sentTimestamp = any()
            )
        }
    }

    @Test
    fun checkVerificationStatus_reloadsUser() = runTest {
        coEvery { authRepository.reloadUser() } returns Result.success(Unit)

        viewModel.checkVerificationStatus()

        coVerify { authRepository.reloadUser() }
    }

    @Test
    fun dismissNudge_recordsPromptShownInManager() = runTest {
        viewModel.dismissNudge()

        coVerify { promptManager.recordPromptShown(PromptType.STUDENT_VERIFICATION) }
    }
}
