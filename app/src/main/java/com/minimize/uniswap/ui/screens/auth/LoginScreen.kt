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
import androidx.hilt.navigation.compose.hiltViewModel

import com.minimize.uniswap.util.LottieAnimationWrapper
import com.minimize.uniswap.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lottie Animation (Placeholder ID, user needs to add a real one to res/raw)
        // LottieAnimationWrapper(
        //    resId = R.raw.login_animation,
        //    modifier = Modifier.size(200.dp)
        // )

        Text(
            text = "Welcome back.",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Log in to your academic identity.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In Button
        OutlinedButton(
            onClick = { viewModel.onGoogleLoginClick("YOUR_WEB_CLIENT_ID") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = !viewModel.isLoading
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google", fontWeight = FontWeight.SemiBold)
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
