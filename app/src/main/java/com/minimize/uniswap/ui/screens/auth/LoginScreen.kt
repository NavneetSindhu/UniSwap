package com.minimize.uniswap.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.*

/**
 * Dedicated Sign In Screen for UniSwap.
 * Matches exact Figma specifications: 40sp WELCOME header, 10sp subtitle, 53dp capsule inputs,
 * primary sign-in action button, and Google sign-in.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val themeColors = UniSwapTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Heading: "WELCOME"
        Text(
            text = stringResource(R.string.welcome_title),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.8).sp,
            color = themeColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Subtitle: "YOUR JOURNEY STARTS HERE"
        Text(
            text = stringResource(R.string.welcome_subtitle),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = themeColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // 3. University Email Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(themeColors.btnBackBg)
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.email.isEmpty()) {
                Text(
                    text = stringResource(R.string.email_placeholder),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.textSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BasicTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.textPrimary,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(themeColors.textPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Password Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(themeColors.btnBackBg)
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.password.isEmpty()) {
                Text(
                    text = stringResource(R.string.password_placeholder),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.textSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BasicTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.textPrimary,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(themeColors.textPrimary),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.onSignInClick()
                }),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Error message feedback
        if (!viewModel.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Primary Sign In Button
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.onSignInClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.textPrimary,
                contentColor = themeColors.background
            ),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color = themeColors.background,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.sign_in_button),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.background
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Google Sign In Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(themeColors.btnBackBg)
                .clickable(enabled = !viewModel.isLoading) {
                    viewModel.onGoogleLoginClick(context)
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.continue_with_google),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Navigation link to Sign Up
        Text(
            text = stringResource(R.string.dont_have_account),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = (-0.28).sp,
            color = themeColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onNavigateToSignUp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            onLoginSuccess()
        }
    }
}
