package com.minimize.uniswap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.minimize.uniswap.ui.MainScreen
import com.minimize.uniswap.ui.MainViewModel
import com.minimize.uniswap.ui.theme.UniSwapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        askNotificationPermission()

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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
