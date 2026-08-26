package com.minimize.uniswap.util

import com.minimize.uniswap.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global Developer & QA debug configuration.
 * Allows toggling simulated production states and QA helpers in debug builds.
 */
object DebugConfig {
    // Controls whether debug features are active (defaults to true in debug builds)
    private val _isDebugModeEnabled = MutableStateFlow(true)
    val isDebugModeEnabled: StateFlow<Boolean> = _isDebugModeEnabled.asStateFlow()

    private val _forceShimmerLoading = MutableStateFlow(false)
    val forceShimmerLoading: StateFlow<Boolean> = _forceShimmerLoading.asStateFlow()

    private val _forceOfflineMode = MutableStateFlow(false)
    val forceOfflineMode: StateFlow<Boolean> = _forceOfflineMode.asStateFlow()

    fun setDebugModeEnabled(enabled: Boolean) {
        _isDebugModeEnabled.value = enabled
    }

    fun setForceShimmer(enabled: Boolean) {
        _forceShimmerLoading.value = enabled
    }

    fun setForceOffline(enabled: Boolean) {
        _forceOfflineMode.value = enabled
    }

    /**
     * Resolves to true ONLY IF:
     * 1. BuildConfig.DEBUG is true (compile-time safety)
     * 2. AND the user has enabled the debug toggle in Developer Settings (runtime switch).
     *
     * In release builds or when toggled off to preview production behavior, this resolves to false.
     */
    fun isDebug(): Boolean {
        return BuildConfig.DEBUG && _isDebugModeEnabled.value
    }

    fun isForceShimmerEnabled(): Boolean {
        return isDebug() && _forceShimmerLoading.value
    }

    fun isForceOffline(): Boolean {
        return isDebug() && _forceOfflineMode.value
    }
}
