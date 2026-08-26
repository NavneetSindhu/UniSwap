package com.minimize.uniswap.ui.screens.list

import android.content.Context
import app.cash.turbine.test
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.UserProfile
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.prompt.GlobalPromptManager
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.util.CloudinaryHelper
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
class ListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val cloudinaryHelper: CloudinaryHelper = mockk(relaxed = true)
    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)
    private val promptManager: GlobalPromptManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var viewModel: ListViewModel

    private val sampleUser = UserProfile(
        uid = "seller_789",
        email = "seller@campus.edu",
        displayName = "Seller Name",
        isEmailVerified = true
    )

    @Before
    fun setUp() {
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)

        viewModel = ListViewModel(
            repository = itemRepository,
            authRepository = authRepository,
            cloudinaryHelper = cloudinaryHelper,
            preferencesManager = preferencesManager,
            promptManager = promptManager,
            context = context
        )
    }

    @Test
    fun titleAndPriceUpdates_updateStateFlows() = runTest {
        viewModel.onTitleChange("Graphing Calculator TI-84")
        viewModel.onPriceChange("1200")
        viewModel.onDescriptionChange("Barely used in semester 1")
        viewModel.onCategorySelected(ItemCategory.ELECTRONICS)

        assertEquals("Graphing Calculator TI-84", viewModel.title.value)
        assertEquals("1200", viewModel.price.value)
        assertEquals("Barely used in semester 1", viewModel.description.value)
        assertEquals(ItemCategory.ELECTRONICS, viewModel.selectedCategory.value)
    }

    @Test
    fun titleExceedingMaxLength_isTruncated() = runTest {
        val longTitle = "A".repeat(80)
        viewModel.onTitleChange(longTitle)

        assertEquals(ListViewModel.MAX_TITLE_LENGTH, viewModel.title.value.length)
    }

    @Test
    fun priceExceedingMaxDigits_isTruncated() = runTest {
        val longPrice = "123456789"
        viewModel.onPriceChange(longPrice)

        assertEquals(ListViewModel.MAX_PRICE_DIGITS, viewModel.price.value.length)
    }

    @Test
    fun postItem_withBlankTitle_setsValidationError() = runTest {
        viewModel.onTitleChange("")
        viewModel.onPriceChange("100")

        viewModel.postItem(onSuccess = {})

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.errorMessage)
            assertFalse(viewModel.isPosting.value)
        }
    }

    @Test
    fun sendVerificationEmail_success_updatesPendingState() = runTest {
        coEvery { authRepository.sendVerificationEmail() } returns Result.success(Unit)

        viewModel.sendVerificationEmail(email = "seller@campus.edu", studentId = "2023EC12")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isVerificationSent)
            assertFalse(state.isProcessingVerification)
        }

        coVerify {
            preferencesManager.updateStudentVerificationDetails(
                collegeEmail = "seller@campus.edu",
                studentId = "2023EC12",
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
}
