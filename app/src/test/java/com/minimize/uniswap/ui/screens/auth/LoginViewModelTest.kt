package com.minimize.uniswap.ui.screens.auth

import android.content.Context
import com.minimize.uniswap.R
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.util.GoogleAuthHelper
import com.minimize.uniswap.util.MainCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val googleAuthHelper: GoogleAuthHelper = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        every { context.getString(R.string.prefix_mr) } returns "Mr."
        every { context.getString(R.string.campus_usar_ggsipu) } returns "USAR (East Delhi Campus)"
        every { context.getString(R.string.branch_cse) } returns "Computer Science & Engineering"
        every { context.getString(R.string.auth_error_enter_email_password) } returns "Please enter both email and password."
        every { context.getString(R.string.auth_error_short_password) } returns "Password must be at least 6 characters."
        every { context.getString(R.string.auth_error_enter_name) } returns "Please enter your full name."
        every { authRepository.isGuestMode } returns MutableStateFlow(false)

        viewModel = LoginViewModel(
            repository = authRepository,
            googleAuthHelper = googleAuthHelper,
            context = context
        )
    }

    @Test
    fun toggleAuthMode_switchesBetweenLoginAndSignUp() {
        assertFalse(viewModel.isSignUpMode)

        viewModel.toggleAuthMode()
        assertTrue(viewModel.isSignUpMode)

        viewModel.toggleAuthMode()
        assertFalse(viewModel.isSignUpMode)
    }

    @Test
    fun onSignInClick_withBlankCredentials_setsErrorMessage() {
        viewModel.email = ""
        viewModel.password = ""

        viewModel.onSignInClick()

        assertNotNull(viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun onSignInClick_withValidCredentials_callsLogin() = runTest {
        viewModel.email = "student@campus.edu"
        viewModel.password = "password123"
        coEvery { authRepository.login(any(), any()) } returns Result.success(Unit)

        viewModel.onSignInClick()

        coVerify { authRepository.login("student@campus.edu", "password123") }
        assertTrue(viewModel.isSuccess)
    }

    @Test
    fun onSignUpClick_withBlankName_setsErrorMessage() {
        viewModel.isSignUpMode = true
        viewModel.name = ""
        viewModel.email = "student@campus.edu"
        viewModel.password = "password123"

        viewModel.onSignUpClick()

        assertNotNull(viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun continueAsGuest_triggersRepositoryGuestMode() = runTest {
        var callbackInvoked = false

        viewModel.continueAsGuest {
            callbackInvoked = true
        }

        coVerify { authRepository.continueAsGuest() }
        assertTrue(callbackInvoked)
    }
}
