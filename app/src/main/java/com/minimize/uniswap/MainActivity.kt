package com.minimize.uniswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.minimize.uniswap.ui.MainScreen
import com.minimize.uniswap.ui.MainViewModel
import com.minimize.uniswap.ui.theme.UniSwapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen on-screen until the preferences are loaded.
        // This prevents the "white flash" on cold starts.
        splashScreen.setKeepOnScreenCondition {
            viewModel.userPreferences.value == null
        }

        enableEdgeToEdge()

        setContent {
            // Observe preferences from ViewModel
            val preferences by viewModel.userPreferences.collectAsState()

            // Only render once preferences are loaded
            preferences?.let { prefs ->
                UniSwapTheme(
                    themeMode = prefs.themeMode,
                    dynamicColor = prefs.dynamicColor,
                    typographyStyle = prefs.typographyStyle
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        MainScreen()
                    }
                }
            }
        }
    }
}
