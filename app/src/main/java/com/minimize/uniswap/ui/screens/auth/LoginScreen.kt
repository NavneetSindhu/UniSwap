package com.minimize.uniswap.ui.screens.auth

import AuthTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minimize.uniswap.data.repository.AuthRepository // Ensure this is imported

@Composable
fun LoginScreen(
    repository: AuthRepository, // 1. Pass the repository from MainScreen
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    // 2. Use the Factory to inject the AuthRepository into the LoginViewModel
    val viewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(repository) as T
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome back.",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Text(
            text = "Log in to your academic identity.",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = "University Email",
            placeholder = "student@university.edu"
        )

        AuthTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = "Password",
            placeholder = "••••••••",
            isPassword = true
        )

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.onLoginClick() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65)),
            shape = RoundedCornerShape(28.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        TextButton(
            onClick = onNavigateToSignup,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Text("Don't have an account? Sign Up", color = Color.Gray)
        }

        // Navigation Logic: Triggers when repository.login() saves the token
        LaunchedEffect(viewModel.isSuccess) {
            // Inside LoginScreen.kt

                if (viewModel.isSuccess) {
                    println("LOGCAT_UI: LaunchedEffect detected isSuccess = true. Calling onLoginSuccess().")
                    onLoginSuccess()
                }
        }
    }
}
