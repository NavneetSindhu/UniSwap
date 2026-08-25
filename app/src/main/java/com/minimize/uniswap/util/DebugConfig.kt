package com.minimize.uniswap.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global Developer & QA debug configuration.
 * Allows toggling simulated states (e.g. forced shimmer skeleton view) in debug builds.
 */
object DebugConfig {
    private val _forceShimmerLoading = MutableStateFlow(false)
    val forceShimmerLoading: StateFlow<Boolean> = _forceShimmerLoading.asStateFlow()

    fun setForceShimmer(enabled: Boolean) {
        _forceShimmerLoading.value = enabled
    }

    fun isForceShimmerEnabled(): Boolean = _forceShimmerLoading.value
}
