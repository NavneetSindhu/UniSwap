package com.minimize.uniswap.data.prompt

import com.minimize.uniswap.data.preferences.UserPreferencesManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Types of proactive user prompts subject to intelligent fatigue control / cooldowns.
 */
enum class PromptType(val defaultCooldownDays: Long) {
    STUDENT_VERIFICATION(defaultCooldownDays = 3),
    RATE_APP(defaultCooldownDays = 14),
    APP_UPDATE(defaultCooldownDays = 1)
}

/**
 * Centralized Prompt Fatigue Engine.
 * Ensures automated background nudges (e.g. passive feed prompts) do not annoy users,
 * while allowing explicit user clicks (e.g. tapping "Verify ID" in Profile) to always open immediately.
 */
@Singleton
class GlobalPromptManager @Inject constructor(
    private val preferencesManager: UserPreferencesManager
) {
    /**
     * Determines whether an automated background prompt is permitted to show based on cooldown rules.
     * Note: Explicit user clicks (e.g. tapping "Verify ID") MUST bypass this and trigger the sheet directly.
     */
    suspend fun canShowPrompt(promptType: PromptType): Boolean {
        val lastShown = preferencesManager.getPromptLastShownTimestamp(promptType.name)
        if (lastShown == 0L) return true
        val now = System.currentTimeMillis()
        val cooldownMillis = TimeUnit.DAYS.toMillis(promptType.defaultCooldownDays)
        return (now - lastShown) >= cooldownMillis
    }

    /**
     * Records that a prompt was shown/dismissed to activate the cooldown period.
     */
    suspend fun recordPromptShown(promptType: PromptType) {
        preferencesManager.updatePromptLastShownTimestamp(promptType.name, System.currentTimeMillis())
    }
}
