package com.minimize.uniswap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class that serves as the entry point for Dagger Hilt.
 * The @HiltAndroidApp annotation triggers Hilt's code generation,
 * including a base class for your application that serves as the
 * application-level dependency container.
 */
@HiltAndroidApp
class UniSwapApplication : Application()
