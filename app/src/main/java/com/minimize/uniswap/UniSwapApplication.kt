package com.minimize.uniswap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

/**
 * Custom Application class that serves as the entry point for Dagger Hilt.
 */
@HiltAndroidApp
class UniSwapApplication : Application() {
    // A scope that lives for the entire application lifecycle
    val applicationScope = CoroutineScope(SupervisorJob())
}
