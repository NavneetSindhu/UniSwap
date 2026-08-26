package com.minimize.uniswap.data.prompt

import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class GlobalPromptManagerTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val preferencesManager: UserPreferencesManager = mockk(relaxed = true)
    private lateinit var promptManager: GlobalPromptManager

    @Before
    fun setUp() {
        promptManager = GlobalPromptManager(preferencesManager)
    }

    @Test
    fun canShowPrompt_returnsTrue_whenPromptNeverShownBefore() = runTest {
        coEvery { preferencesManager.getPromptLastShownTimestamp(PromptType.STUDENT_VERIFICATION.name) } returns 0L

        val canShow = promptManager.canShowPrompt(PromptType.STUDENT_VERIFICATION)

        assertTrue(canShow)
    }

    @Test
    fun canShowPrompt_returnsFalse_whenWithinCooldownPeriod() = runTest {
        // Shown 1 day ago (cooldown is 3 days)
        val oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        coEvery { preferencesManager.getPromptLastShownTimestamp(PromptType.STUDENT_VERIFICATION.name) } returns oneDayAgo

        val canShow = promptManager.canShowPrompt(PromptType.STUDENT_VERIFICATION)

        assertFalse(canShow)
    }

    @Test
    fun canShowPrompt_returnsTrue_whenCooldownExpired() = runTest {
        // Shown 4 days ago (cooldown is 3 days)
        val fourDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4)
        coEvery { preferencesManager.getPromptLastShownTimestamp(PromptType.STUDENT_VERIFICATION.name) } returns fourDaysAgo

        val canShow = promptManager.canShowPrompt(PromptType.STUDENT_VERIFICATION)

        assertTrue(canShow)
    }

    @Test
    fun recordPromptShown_updatesPreferencesWithCurrentTimestamp() = runTest {
        promptManager.recordPromptShown(PromptType.STUDENT_VERIFICATION)

        coVerify {
            preferencesManager.updatePromptLastShownTimestamp(
                PromptType.STUDENT_VERIFICATION.name,
                more(0L)
            )
        }
    }
}
