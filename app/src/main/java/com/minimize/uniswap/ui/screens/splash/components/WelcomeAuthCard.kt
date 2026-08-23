package com.minimize.uniswap.ui.screens.splash.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.*

/**
 * Welcome & Auth Card matching exact Figma CSS specs (iPhone 16 & 17 Pro - 11).
 * Features:
 * - 40sp WELCOME header & 10sp YOUR JOURNEY STARTS HERE subtitle
 * - 53dp height #30353B capsule fields for Email and Password
 * - 53dp #EDEDED primary button for Sign in / Sign Up
 * - 53dp #EDEDED Google single sign-on capsule
 * - Seamless toggle between Sign In and Sign Up modes
 * - Legal disclaimer in 8-9sp #767676
 */
@Composable
fun WelcomeAuthCard(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    isSignUpMode: Boolean,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val themeColors = UniSwapTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Heading: "WELCOME" / "CREATE ACCOUNT" (Matter Black 40sp)
        Text(
            text = stringResource(if (isSignUpMode) R.string.create_account_title else R.string.welcome_title),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = if (isSignUpMode) 32.sp else 40.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.8).sp,
            color = themeColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Subtitle: "YOUR JOURNEY STARTS HERE" (Matter Bold 10sp, letter-spacing 2sp)
        Text(
            text = stringResource(if (isSignUpMode) R.string.create_account_subtitle else R.string.welcome_subtitle),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = themeColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Optional Full Name Field for Sign Up mode (Rectangle 2: height 53dp, #30353B, radius 50)
        AnimatedVisibility(
            visible = isSignUpMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                AuthCapsuleTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = stringResource(R.string.name_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 4. Email Field (Rectangle 2: height 53dp, #30353B, radius 50)
        AuthCapsuleTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.email_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Password Field (Rectangle 7: height 53dp, #30353B, radius 50)
        AuthCapsuleTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.password_placeholder),
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onSubmit()
            }),
            modifier = Modifier.fillMaxWidth()
        )

        // Error message feedback
        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Thin Divider (Line 1: #424242)
        HorizontalDivider(
            thickness = 1.dp,
            color = PaletteDark.Gray700,
            modifier = Modifier.fillMaxWidth(0.95f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Primary Action Button (Rectangle 4 / 5: height 53dp, #EDEDED, radius 50)
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NavIndicatorBg,
                contentColor = PaletteLight.Gray950
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = PaletteLight.Gray950,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = stringResource(if (isSignUpMode) R.string.sign_up_button else R.string.sign_in_button),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    color = PaletteLight.Gray950
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. Google Sign-In Capsule (Rectangle 6: height 53dp, #EDEDED, radius 50)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(NavIndicatorBg)
                .clickable(enabled = !isLoading, onClick = onGoogleSignInClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.continue_with_google),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    color = PaletteLight.Gray950
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 9. Toggle Mode Link ("Sign Up" / "Already have an account? Sign in")
        Text(
            text = stringResource(if (isSignUpMode) R.string.already_have_account else R.string.dont_have_account),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = (-0.28).sp,
            color = TextPriceDisplay,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onToggleMode)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 10. Legal Terms Disclaimer
        val andString = stringResource(R.string.terms_and)
        val disclaimerText = buildAnnotatedString {
            append(stringResource(R.string.terms_prefix))
            append("\n")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Medium,
                    color = TextPriceDisplay
                )
            ) {
                append(stringResource(R.string.terms_of_service))
            }
            append(andString)
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Medium,
                    color = TextPriceDisplay
                )
            ) {
                append(stringResource(R.string.privacy_policy))
            }
        }

        Text(
            text = disclaimerText,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            lineHeight = 13.sp,
            letterSpacing = (-0.18).sp,
            color = TextMutedLight,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Reusable Capsule Text Field matching Figma CSS:
 * Height: 53dp, Shape: RoundedCornerShape(50dp), Fill: #30353B.
 * Placeholder: Matter Medium 14sp #BBBBBB. Text: Matter Medium 14sp White.
 */
@Composable
private fun AuthCapsuleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Box(
        modifier = modifier
            .height(53.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(BtnBackBg)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = (-0.28).sp,
                color = AuthFieldPlaceholderColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontSize = 14.sp,
                letterSpacing = (-0.28).sp,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color.White),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
