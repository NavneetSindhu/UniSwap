package com.minimize.uniswap.ui.screens.settings

import app.cash.turbine.test
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.preferences.ThemeMode
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
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
class SettingsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val reportRepository: ReportRepository = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    private val samplePreferences = UserPreferences(
        themeMode = ThemeMode.SYSTEM,
        dynamicColor = true,
        campusCenter = "East Campus"
    )

    private val sampleUser = User(
        uid = "user_settings_1",
        email = "settings@campus.edu",
        displayName = "Settings User"
    )

    @Before
    fun setUp() {
        every { preferencesManager.preferencesFlow } returns flowOf(samplePreferences)
        every { authRepository.getCurrentUserId() } returns "user_settings_1"
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)
        every { reportRepository.getBlockedUserIdsFlow() } returns MutableStateFlow(setOf("user_blocked_1"))
        every { reportRepository.getMyReportsFlow() } returns flowOf(emptyList())

        viewModel = SettingsViewModel(
            preferencesManager = preferencesManager,
            authRepository = authRepository,
            reportRepository = reportRepository
        )
    }

    @Test
    fun preferences_emitsCorrectInitialPreferences() = runTest {
        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
            assertTrue(prefs.dynamicColor)
            assertEquals("East Campus", prefs.campusCenter)
        }
    }

    @Test
    fun onThemeModeChanged_updatesPreferencesManager() = runTest {
        viewModel.onThemeModeChanged(ThemeMode.DARK)

        coVerify { preferencesManager.updateThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun onDynamicColorChanged_updatesPreferencesManager() = runTest {
        viewModel.onDynamicColorChanged(false)

        coVerify { preferencesManager.updateDynamicColor(false) }
    }

    @Test
    fun onCampusCenterChanged_updatesPreferencesAndAuthRepository() = runTest {
        viewModel.onCampusCenterChanged("West Campus")

        coVerify { preferencesManager.updateCampusCenter("West Campus") }
        coVerify { authRepository.updateCampusCenter("West Campus") }
    }

    @Test
    fun unblockUser_callsReportRepository() = runTest {
        coEvery { reportRepository.unblockUser("user_blocked_1") } returns Result.success(Unit)

        var callbackResult = false
        viewModel.unblockUser("user_blocked_1") { success ->
            callbackResult = success
        }

        coVerify { reportRepository.unblockUser("user_blocked_1") }
        assertTrue(callbackResult)
    }

    @Test
    fun logout_callsAuthRepository() = runTest {
        var logoutCompleted = false

        viewModel.logout {
            logoutCompleted = true
        }

        coVerify { authRepository.logout() }
        assertTrue(logoutCompleted)
    }
}
