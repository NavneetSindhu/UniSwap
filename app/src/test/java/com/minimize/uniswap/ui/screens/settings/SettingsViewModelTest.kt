package com.minimize.uniswap.ui.screens.settings

import app.cash.turbine.test
import com.minimize.uniswap.data.model.UserProfile
import com.minimize.uniswap.data.preferences.ThemeMode
import com.minimize.uniswap.data.preferences.TypographyStyle
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ReportRepository
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
class SettingsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val reportRepository: ReportRepository = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    private val initialPreferences = UserPreferences(
        themeMode = ThemeMode.SYSTEM,
        dynamicColor = false,
        typographyStyle = TypographyStyle.MODERN,
        campusCenter = "Main Campus"
    )

    private val sampleUser = UserProfile(
        uid = "user_settings_123",
        email = "settings@campus.edu",
        displayName = "Settings User"
    )

    @Before
    fun setUp() {
        every { preferencesManager.preferencesFlow } returns MutableStateFlow(initialPreferences)
        every { authRepository.getUserFlow() } returns MutableStateFlow(sampleUser)
        every { reportRepository.getBlockedUserIdsFlow() } returns MutableStateFlow(setOf("blocked_1"))
        every { reportRepository.getMyReportsFlow() } returns MutableStateFlow(emptyList())

        viewModel = SettingsViewModel(
            preferencesManager = preferencesManager,
            authRepository = authRepository,
            reportRepository = reportRepository
        )
    }

    @Test
    fun onThemeModeChanged_updatesPreferences() = runTest {
        viewModel.onThemeModeChanged(ThemeMode.DARK)

        coVerify { preferencesManager.updateThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun onDynamicColorChanged_updatesPreferences() = runTest {
        viewModel.onDynamicColorChanged(true)

        coVerify { preferencesManager.updateDynamicColor(true) }
    }

    @Test
    fun onTypographyStyleChanged_updatesPreferences() = runTest {
        viewModel.onTypographyStyleChanged(TypographyStyle.EDITORIAL)

        coVerify { preferencesManager.updateTypographyStyle(TypographyStyle.EDITORIAL) }
    }

    @Test
    fun onCampusCenterChanged_updatesBothPreferencesAndAuth() = runTest {
        viewModel.onCampusCenterChanged("North Campus")

        coVerify {
            preferencesManager.updateCampusCenter("North Campus")
            authRepository.updateCampusCenter("North Campus")
        }
    }

    @Test
    fun unblockUser_success_updatesFeedbackMessage() = runTest {
        coEvery { reportRepository.unblockUser("blocked_1") } returns Result.success(Unit)

        viewModel.unblockUser("blocked_1")

        viewModel.userFeedbackMessage.test {
            val msg = awaitItem()
            assertEquals("User unblocked successfully.", msg)
        }
    }

    @Test
    fun logout_callsAuthRepositoryAndExecutesCallback() = runTest {
        coEvery { authRepository.logout() } returns Result.success(Unit)
        var callbackExecuted = false

        viewModel.logout { callbackExecuted = true }

        coVerify { authRepository.logout() }
        assertTrue(callbackExecuted)
    }

    @Test
    fun deleteAccount_success_updatesFeedbackAndExecutesCallback() = runTest {
        coEvery { authRepository.deleteAccount() } returns Result.success(Unit)
        var callbackExecuted = false

        viewModel.deleteAccount { callbackExecuted = true }

        coVerify { authRepository.deleteAccount() }
        assertTrue(callbackExecuted)
        assertEquals("Your account has been deleted.", viewModel.userFeedbackMessage.value)
    }
}
