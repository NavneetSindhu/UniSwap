package com.minimize.uniswap.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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

    var showPrefixSheet by remember { mutableStateOf(false) }
    var showCollegeSheet by remember { mutableStateOf(false) }
    var showBranchSheet by remember { mutableStateOf(false) }
    var showBatchSheet by remember { mutableStateOf(false) }
    var showTermsSheet by remember { mutableStateOf(false) }

    val prefixOptions = remember {
        listOf(
            context.getString(R.string.prefix_mr),
            context.getString(R.string.prefix_ms),
            context.getString(R.string.prefix_mx)
        )
    }

    val collegeOptions = remember {
        listOf(
            context.getString(R.string.campus_usar_ggsipu),
            context.getString(R.string.campus_ggsipu),
            context.getString(R.string.campus_pu),
            context.getString(R.string.campus_pec),
            context.getString(R.string.campus_uiet),
            context.getString(R.string.campus_chitkara),
            context.getString(R.string.campus_thapar),
            context.getString(R.string.campus_iit_ropar)
        )
    }

    val branchOptions = remember {
        listOf(
            context.getString(R.string.branch_automation_robotics),
            context.getString(R.string.branch_aiml),
            context.getString(R.string.branch_aids),
            context.getString(R.string.branch_iiot),
            context.getString(R.string.branch_cse),
            context.getString(R.string.branch_it),
            context.getString(R.string.branch_ece),
            context.getString(R.string.branch_ee),
            context.getString(R.string.branch_me),
            context.getString(R.string.branch_ce),
            context.getString(R.string.branch_ba),
            context.getString(R.string.branch_design),
            context.getString(R.string.branch_biotech),
            context.getString(R.string.branch_other)
        )
    }

    val batchOptions = remember {
        listOf("2024", "2025", "2026", "2027", "2028", "2029", "2030")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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

        // 3. Full Name with Title Prefix Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(53.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(themeColors.btnBackBg)
                    .clickable {
                        focusManager.clearFocus()
                        showPrefixSheet = true
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = viewModel.namePrefix,
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = themeColors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.select_prefix),
                        tint = themeColors.textSubtle,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AuthCapsuleInputField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                placeholder = stringResource(R.string.name_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }

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
            label = stringResource(R.string.label_college_campus),
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
                label = stringResource(R.string.label_branch),
                value = viewModel.branch,
                onClick = {
                    focusManager.clearFocus()
                    showBranchSheet = true
                },
                modifier = Modifier.weight(1.3f)
            )

            AuthCapsuleDropdownField(
                label = stringResource(R.string.label_batch),
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

        val primaryButtonBg by animateColorAsState(
            targetValue = if (viewModel.isSuccess && !viewModel.isGoogleLoading) {
                themeColors.success
            } else {
                themeColors.textPrimary
            },
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "SignUpPrimaryButtonBg"
        )

        val googleButtonBg by animateColorAsState(
            targetValue = if (viewModel.isSuccess && viewModel.isGoogleLoading) {
                themeColors.success
            } else {
                themeColors.btnBackBg
            },
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "SignUpGoogleButtonBg"
        )

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
                containerColor = primaryButtonBg,
                contentColor = if (viewModel.isSuccess && !viewModel.isGoogleLoading) Color.White else themeColors.background
            ),
            enabled = !viewModel.isLoading && !viewModel.isSuccess
        ) {
            if (viewModel.isSuccess && !viewModel.isGoogleLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sign_up_success),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.28).sp,
                        color = Color.White
                    )
                }
            } else if (viewModel.isEmailLoading) {
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
                .background(googleButtonBg)
                .clickable(enabled = !viewModel.isLoading && !viewModel.isSuccess) {
                    viewModel.onGoogleLoginClick(context)
                },
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isSuccess && viewModel.isGoogleLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sign_up_success),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = (-0.28).sp,
                        color = Color.White
                    )
                }
            } else if (viewModel.isGoogleLoading) {
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

        // 11. Terms of Service & Privacy Policy Notice
        val termsNotice = buildAnnotatedString {
            append(stringResource(R.string.terms_prefix).trim())
            append(" ")
            withStyle(SpanStyle(color = themeColors.textPrimary, fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.terms_of_service).trim())
            }
            append(" ")
            append(stringResource(R.string.terms_and).trim())
            append(" ")
            withStyle(SpanStyle(color = themeColors.textPrimary, fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.privacy_policy).trim())
            }
        }

        Text(
            text = termsNotice,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = themeColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { showTermsSheet = true }
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Prefix Bottom Sheet
    if (showPrefixSheet) {
        AppBottomSheet(
            onDismissRequest = { showPrefixSheet = false },
            heightFraction = 0.40f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_prefix),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(prefixOptions) { option ->
                        val isSelected = viewModel.namePrefix == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.namePrefix = option
                                    showPrefixSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontFamily = MatterFontFamily,
                                fontSize = 15.sp,
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
                    text = stringResource(R.string.settings_select_campus),
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
            heightFraction = 0.65f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_branch),
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
                    text = stringResource(R.string.select_batch),
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
                                text = stringResource(R.string.label_class_of, option),
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

    // Terms & Privacy Bottom Sheet
    if (showTermsSheet) {
        AppBottomSheet(
            onDismissRequest = { showTermsSheet = false },
            heightFraction = 0.55f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.settings_terms_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = stringResource(R.string.settings_terms_body),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = themeColors.textSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(550)
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
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(53.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(themeColors.btnBackBg)
            .padding(start = 22.dp, end = if (isPassword) 12.dp else 22.dp),
        contentAlignment = Alignment.CenterStart
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
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isPassword) Modifier.padding(end = 36.dp) else Modifier)
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
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isPassword) Modifier.padding(end = 36.dp) else Modifier)
        )

        if (isPassword) {
            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = stringResource(R.string.password_placeholder),
                    tint = themeColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = themeColors.textSubtle
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    color = themeColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
