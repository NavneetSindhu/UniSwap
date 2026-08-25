package com.minimize.uniswap

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Inject

/**
 * Custom Application class that serves as the entry point for Dagger Hilt.
 * Initializes logging trees (Timber in Debug, Crashlytics in Release).
 */
@HiltAndroidApp
class UniSwapApplication : Application() {
    // A scope that lives for the entire application lifecycle
    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // Debug: Plant Timber.DebugTree to log to Logcat with automatic class tags
            Timber.plant(Timber.DebugTree())
        } else {
            // Release / Production: Plant CrashlyticsTree to capture errors without polluting Logcat
            Timber.plant(CrashlyticsTree())
        }
    }
}

/**
 * Production Tree: Routes Error & Warning logs directly to Firebase Crashlytics
 * with zero Logcat output or device overhead.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            try {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log("[${tag ?: "UniSwap"}] $message")
                if (t != null) {
                    crashlytics.recordException(t)
                }
            } catch (_: Exception) {
                // Ignore if Firebase Crashlytics is unavailable in test environments
            }
        }
    }
}
