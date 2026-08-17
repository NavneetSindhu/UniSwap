package com.minimize.uniswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.minimize.uniswap.ui.MainScreen
import com.minimize.uniswap.ui.theme.UniSwapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            UniSwapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    // MainScreen handles the Bottom Nav and Navigation between
                    // Feed, Details, Profile, Chat, and Sell.
                    MainScreen()
                }
            }
        }
    }
}
