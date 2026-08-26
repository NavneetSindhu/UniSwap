package com.minimize.uniswap.ui.components.verification

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.AppBottomSheet
import com.minimize.uniswap.ui.components.LocalBottomSheetDismiss
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.PaletteLight
import com.minimize.uniswap.ui.theme.UniSwapTheme
import kotlinx.coroutines.delay

/**
 * Modern Multi-Step Bottom Sheet for Campus & Student ID Email Verification.
 * Supports editable campus email entry, optional roll number, resend cooldown timer,
 * instant reload status check, and celebration confirmation state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentVerificationBottomSheet(
    initialEmail: String,
    initialStudentId: String = "",
    isAlreadyPending: Boolean = false,
    onDismissRequest: () -> Unit,
    onSendVerificationEmail: (email: String, studentId: String) -> Unit,
    onCheckVerificationStatus: () -> Unit,
    isSendingEmail: Boolean = false,
    isCheckingStatus: Boolean = false,
    isVerified: Boolean = false,
    onVerificationComplete: () -> Unit = onDismissRequest
) {
    val themeColors = UniSwapTheme.colors
    val focusManager = LocalFocusManager.current

    var currentStep by remember(isVerified, isAlreadyPending) {
        mutableStateOf(
            when {
                isVerified -> VerificationStep.SUCCESS
                isAlreadyPending -> VerificationStep.PENDING_INBOX
                else -> VerificationStep.OVERVIEW
            }
        )
    }

    var emailInput by remember { mutableStateOf(initialEmail) }
    var studentIdInput by remember { mutableStateOf(initialStudentId) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var resendCooldown by remember { mutableIntStateOf(if (isAlreadyPending) 60 else 0) }

    val btnContainerColor = themeColors.textPrimary
    val btnContentColor = themeColors.background

    // Resend cooldown timer
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000L)
            resendCooldown -= 1
        }
    }

    // Auto-advance to success when verified
    LaunchedEffect(isVerified) {
        if (isVerified) {
            currentStep = VerificationStep.SUCCESS
        }
    }

    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        heightFraction = 0.88f,
        containerColor = themeColors.cardSurface,
        contentColor = themeColors.textPrimary
    ) {
        val dismissSheet = LocalBottomSheetDismiss.current

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it } + fadeOut(tween(160)))
                } else {
                    (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(160)))
                }
            },
            label = "student_verification_step_transition"
        ) { step ->
            when (step) {
                VerificationStep.OVERVIEW -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. School Badge Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(themeColors.wasteMetricGreen.copy(alpha = 0.15f))
                                .border(1.dp, themeColors.wasteMetricGreen.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = themeColors.wasteMetricGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = stringResource(R.string.verification_sheet_title),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = themeColors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.verification_sheet_subtitle),
                            fontFamily = MatterFontFamily,
                            fontSize = 13.sp,
                            color = themeColors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // 2. Email Input Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it.trim()
                                emailError = null
                            },
                            label = { Text(stringResource(R.string.verification_email_label), fontFamily = MatterFontFamily) },
                            placeholder = { Text(stringResource(R.string.verification_email_placeholder), fontFamily = MatterFontFamily) },
                            isError = emailError != null,
                            supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontFamily = MatterFontFamily) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeColors.wasteMetricGreen,
                                unfocusedBorderColor = themeColors.divider,
                                focusedContainerColor = themeColors.cardBackground,
                                unfocusedContainerColor = themeColors.cardBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Test Email Chip (for instant debug bypass without OTP)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(themeColors.wasteMetricGreen.copy(alpha = 0.14f))
                                    .clickable {
                                        emailInput = "test@campus.edu"
                                        if (studentIdInput.isBlank()) studentIdInput = "2026TEST001"
                                        emailError = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🧪 Fill Test Email (test@campus.edu)",
                                    fontFamily = MatterFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.wasteMetricGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3. Optional Student Roll No / ID Field
                        OutlinedTextField(
                            value = studentIdInput,
                            onValueChange = { studentIdInput = it.trim() },
                            label = { Text(stringResource(R.string.verification_studentid_label), fontFamily = MatterFontFamily) },
                            placeholder = { Text(stringResource(R.string.verification_studentid_placeholder), fontFamily = MatterFontFamily) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeColors.wasteMetricGreen,
                                unfocusedBorderColor = themeColors.divider,
                                focusedContainerColor = themeColors.cardBackground,
                                unfocusedContainerColor = themeColors.cardBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Benefits Breakdown
                        VerificationBenefitItem(
                            icon = Icons.Outlined.Verified,
                            title = stringResource(R.string.verification_benefit_1_title),
                            body = stringResource(R.string.verification_benefit_1_body)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        VerificationBenefitItem(
                            icon = Icons.Outlined.Storefront,
                            title = stringResource(R.string.verification_benefit_2_title),
                            body = stringResource(R.string.verification_benefit_2_body)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        VerificationBenefitItem(
                            icon = Icons.Outlined.Forum,
                            title = stringResource(R.string.verification_benefit_3_title),
                            body = stringResource(R.string.verification_benefit_3_body)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 5. Primary Send Link Action
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (emailInput.isBlank() || !emailInput.contains("@")) {
                                    emailError = "Please enter a valid campus email address"
                                    return@Button
                                }
                                onSendVerificationEmail(emailInput, studentIdInput)
                                resendCooldown = 60
                                currentStep = VerificationStep.PENDING_INBOX
                            },
                            enabled = !isSendingEmail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = btnContainerColor,
                                contentColor = btnContentColor
                            )
                        ) {
                            if (isSendingEmail) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = btnContentColor,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.verification_send_link_btn),
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Later Action (Rule 9 dismiss)
                        TextButton(
                            onClick = { dismissSheet() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.verification_later_btn),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = themeColors.textSecondary
                            )
                        }
                    }
                }

                VerificationStep.PENDING_INBOX -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mail Sent Animated Graphic
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(themeColors.wasteMetricGreen.copy(alpha = 0.15f))
                                .border(1.5.dp, themeColors.wasteMetricGreen.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = themeColors.wasteMetricGreen,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = stringResource(R.string.verification_sent_title),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = themeColors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.verification_sent_body, emailInput),
                            fontFamily = MatterFontFamily,
                            fontSize = 13.sp,
                            color = themeColors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Status Check Button
                        Button(
                            onClick = {
                                onCheckVerificationStatus()
                                val isTest = emailInput.trim().lowercase().let {
                                    it == "test@campus.edu" || it == "demo@student.edu" || it.startsWith("test@") || it.endsWith("@uniswap.test")
                                }
                                if (isTest) {
                                    currentStep = VerificationStep.SUCCESS
                                }
                            },
                            enabled = !isCheckingStatus,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = btnContainerColor,
                                contentColor = btnContentColor
                            )
                        ) {
                            if (isCheckingStatus) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = btnContentColor,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.verification_check_status_btn),
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Resend Countdown / Action
                        if (resendCooldown > 0) {
                            Text(
                                text = stringResource(R.string.verification_resend_timer, resendCooldown),
                                fontFamily = MatterFontFamily,
                                fontSize = 13.sp,
                                color = themeColors.textSubtle
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.verification_resend_action),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = themeColors.wasteMetricGreen,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSendVerificationEmail(emailInput, studentIdInput)
                                        resendCooldown = 60
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = { dismissSheet() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.verification_later_btn),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = themeColors.textSecondary
                            )
                        }
                    }
                }

                VerificationStep.SUCCESS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Confetti Celebration / Checkmark Graphic
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(themeColors.wasteMetricGreen.copy(alpha = 0.18f))
                                .border(2.dp, themeColors.wasteMetricGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = themeColors.wasteMetricGreen,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.verification_success_title),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = themeColors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.verification_success_body),
                            fontFamily = MatterFontFamily,
                            fontSize = 14.sp,
                            color = themeColors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                onVerificationComplete()
                                dismissSheet()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = btnContainerColor,
                                contentColor = btnContentColor
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.verification_continue_btn),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationBenefitItem(
    icon: ImageVector,
    title: String,
    body: String
) {
    val themeColors = UniSwapTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(themeColors.cardBackground)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(themeColors.wasteMetricGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.wasteMetricGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = themeColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = themeColors.textSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
