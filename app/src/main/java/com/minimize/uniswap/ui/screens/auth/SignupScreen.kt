package com.minimize.uniswap.ui.screens.auth

import AuthTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minimize.uniswap.data.repository.AuthRepository // Import Repo

@Composable
fun SignupScreen(
    repository: AuthRepository, // 1. Added Repository Parameter
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit // Added for the "Back to Login" button
) {
    // 2. Initialize ViewModel with a Factory to pass the Repository
    val viewModel: SignupViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SignupViewModel(repository) as T
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- TOP PROGRESS SECTION ---
        Text(
            text = "STEP 01 OF 03",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { 0.33f },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = Color(0xFFFF8A65),
            trackColor = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- HEADING ---
        Text(
            text = "Create your\nacademic identity.",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 40.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- INPUT FIELDS ---
        AuthTextField(
            value = viewModel.fullName,
            onValueChange = { viewModel.fullName = it },
            label = "Full Name",
            placeholder = "Alex Rivers"
        )
        AuthTextField(
            value = viewModel.department,
            onValueChange = { viewModel.department = it },
            label = "Department",
            placeholder = "Select Department"
        )
        AuthTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = "University Email",
            placeholder = "student@university.edu"
        )
        AuthTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = "Create Password",
            placeholder = "••••••••",
            isPassword = true
        )

        // Error Message
        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- SUBMIT BUTTON ---
        Button(
            onClick = { viewModel.onSignupClick() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65)),
            shape = RoundedCornerShape(28.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Join the Community", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // --- LOGIN REDIRECT ---
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Already have an account? Sign In", color = Color.Gray)
        }

        // --- NAVIGATION EFFECT ---
        LaunchedEffect(viewModel.isSuccess) {
            if (viewModel.isSuccess) {
                onSignupSuccess()
            }
        }
    }
}
