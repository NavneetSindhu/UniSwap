package com.minimize.uniswap.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.minimize.uniswap.ui.components.AppBottomSheet
import com.minimize.uniswap.ui.theme.*

/**
 * Dedicated Sign Up Screen for UniSwap.
 * Collects Full Name, University Email, Password, and dropdown selections for College, Branch, and Batch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val themeColors = UniSwapTheme.colors

    var showCollegeSheet by remember { mutableStateOf(false) }
    var showBranchSheet by remember { mutableStateOf(false) }
    var showBatchSheet by remember { mutableStateOf(false) }

    val collegeOptions = remember {
        listOf(
            "Main Campus Center",
            "North Engineering Quad",
            "South Campus Science Complex",
            "Health & Medical Campus",
            "Business & Management Center",
            "Downtown Tech Campus"
        )
    }

    val branchOptions = remember {
        listOf(
            "Computer Science & Engineering",
            "Information Technology",
            "Electronics & Communication",
            "Mechanical Engineering",
            "Civil Engineering",
            "Business Administration",
            "Design & Architecture",
            "Biotechnology",
            "Other / Undeclared"
        )
    }

    val batchOptions = remember {
        listOf("2024", "2025", "2026", "2027", "2028", "2029")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Heading: "CREATE ACCOUNT"
        Text(
            text = stringResource(R.string.create_account_title),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.8).sp,
            color = themeColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Subtitle: "YOUR JOURNEY STARTS HERE"
        Text(
            text = stringResource(R.string.create_account_subtitle),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = themeColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Full Name
        AuthCapsuleInputField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            placeholder = stringResource(R.string.name_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. University Email
        AuthCapsuleInputField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            placeholder = stringResource(R.string.email_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Password
        AuthCapsuleInputField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            placeholder = stringResource(R.string.password_placeholder),
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 6. College / Campus Center Dropdown
        AuthCapsuleDropdownField(
            label = "College / Campus",
            value = viewModel.college,
            onClick = {
                focusManager.clearFocus()
                showCollegeSheet = true
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 7. Branch & Batch Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuthCapsuleDropdownField(
                label = "Branch",
                value = viewModel.branch,
                onClick = {
                    focusManager.clearFocus()
                    showBranchSheet = true
                },
                modifier = Modifier.weight(1.3f)
            )

            AuthCapsuleDropdownField(
                label = "Batch",
                value = viewModel.batch,
                onClick = {
                    focusManager.clearFocus()
                    showBatchSheet = true
                },
                modifier = Modifier.weight(0.7f)
            )
        }

        // Error message
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

        Spacer(modifier = Modifier.height(20.dp))

        // 8. Create Account Primary Button
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.onSignUpClick()
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
            if (viewModel.isEmailLoading) {
                CircularProgressIndicator(
                    color = themeColors.background,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.sign_up_button),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.28).sp,
                    color = themeColors.background
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 9. Google Sign In Button
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
            if (viewModel.isGoogleLoading) {
                CircularProgressIndicator(
                    color = themeColors.textPrimary,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
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
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 10. Switch to Sign In link
        Text(
            text = stringResource(R.string.already_have_account),
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = (-0.28).sp,
            color = themeColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onNavigateToSignIn)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // College Bottom Sheet
    if (showCollegeSheet) {
        AppBottomSheet(
            onDismissRequest = { showCollegeSheet = false },
            heightFraction = 0.55f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Select College / Campus",
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(collegeOptions) { option ->
                        val isSelected = viewModel.college == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.college = option
                                    showCollegeSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontFamily = MatterFontFamily,
                                fontSize = 14.sp,
                                color = if (isSelected) themeColors.textPrimary else themeColors.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = themeColors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Branch Bottom Sheet
    if (showBranchSheet) {
        AppBottomSheet(
            onDismissRequest = { showBranchSheet = false },
            heightFraction = 0.60f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Select Branch / Major",
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(branchOptions) { option ->
                        val isSelected = viewModel.branch == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.branch = option
                                    showBranchSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontFamily = MatterFontFamily,
                                fontSize = 14.sp,
                                color = if (isSelected) themeColors.textPrimary else themeColors.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = themeColors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Batch Bottom Sheet
    if (showBatchSheet) {
        AppBottomSheet(
            onDismissRequest = { showBatchSheet = false },
            heightFraction = 0.50f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Select Graduation Year",
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(batchOptions) { option ->
                        val isSelected = viewModel.batch == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.batch = option
                                    showBatchSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Class of $option",
                                fontFamily = MatterFontFamily,
                                fontSize = 14.sp,
                                color = if (isSelected) themeColors.textPrimary else themeColors.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = themeColors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            onSignUpSuccess()
        }
    }
}

/**
 * Capsule text field for Auth matching Figma styling
 */
@Composable
private fun AuthCapsuleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val themeColors = UniSwapTheme.colors

    Box(
        modifier = modifier
            .height(53.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(themeColors.btnBackBg)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
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
            value = value,
            onValueChange = onValueChange,
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
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Capsule dropdown selector field matching Figma styling
 */
@Composable
private fun AuthCapsuleDropdownField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors

    Box(
        modifier = modifier
            .height(53.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(themeColors.btnBackBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = themeColors.textSubtle
                )
                Text(
                    text = value,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = themeColors.textPrimary,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = themeColors.textSubtle,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
