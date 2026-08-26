package com.minimize.uniswap.ui.screens.list

import android.content.Context
import app.cash.turbine.test
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.User
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

    private val sampleUser = User(
        uid = "user_list_1",
        email = "seller@campus.edu",
        displayName = "Student Seller",
        isEmailVerified = false
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user_list_1"
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
    fun onTitleChange_capsAtMaxLength() {
        val longTitle = "A".repeat(80)
        viewModel.onTitleChange(longTitle)

        // Should not accept length > 60
        assertTrue(viewModel.title.value.length <= ListViewModel.MAX_TITLE_LENGTH)
    }

    @Test
    fun onPriceChange_acceptsDigitsOnly() {
        viewModel.onPriceChange("500abc")
        assertEquals("500", viewModel.price.value)
    }

    @Test
    fun onCategoryChange_updatesSelectedCategory() = runTest {
        viewModel.onCategoryChange(ItemCategory.ELECTRONICS)

        viewModel.selectedCategory.test {
            val category = awaitItem()
            assertEquals(ItemCategory.ELECTRONICS, category)
        }
    }

    @Test
    fun onPostAttempt_withEmptyTitle_setsErrorMessage() = runTest {
        viewModel.onTitleChange("")
        viewModel.onPriceChange("200")
        viewModel.onCategoryChange(ItemCategory.DORM_ESSENTIALS)

        var successInvoked = false
        viewModel.onPostAttempt {
            successInvoked = true
        }

        assertFalse(successInvoked)
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.errorMessage)
        }
    }

    @Test
    fun sendVerificationEmail_callsAuthRepository() = runTest {
        coEvery { authRepository.sendVerificationEmail() } returns Result.success(Unit)

        viewModel.sendVerificationEmail("seller@campus.edu", "2023CSE01")

        coVerify { authRepository.sendVerificationEmail() }
    }
}
