package com.minimize.uniswap.ui.components.nudge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.AppBottomSheet
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Modern Bottom Sheet Nudge for Unverified College Students.
 * Explains student verification benefits and triggers the verification email workflow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationNudgeDialog(
    onDismiss: () -> Unit,
    onVerifyClick: () -> Unit
) {
    val themeColors = UniSwapTheme.colors

    AppBottomSheet(
        onDismissRequest = onDismiss,
        heightFraction = 0.85f,
        containerColor = themeColors.cardSurface,
        contentColor = themeColors.textPrimary
    ) {
        val dismissSheet = com.minimize.uniswap.ui.components.LocalBottomSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(themeColors.wasteMetricGreen.copy(alpha = 0.15f))
                    .border(1.dp, themeColors.wasteMetricGreen.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = themeColors.wasteMetricGreen,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Benefit 1: Badge
            VerificationBenefitRow(
                icon = Icons.Outlined.VerifiedUser,
                title = stringResource(R.string.verification_benefit_1_title),
                body = stringResource(R.string.verification_benefit_1_body),
                tint = themeColors.wasteMetricGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Benefit 2: Listings
            VerificationBenefitRow(
                icon = Icons.Outlined.Inventory2,
                title = stringResource(R.string.verification_benefit_2_title),
                body = stringResource(R.string.verification_benefit_2_body),
                tint = themeColors.wasteMetricGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Benefit 3: Chat
            VerificationBenefitRow(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = stringResource(R.string.verification_benefit_3_title),
                body = stringResource(R.string.verification_benefit_3_body),
                tint = themeColors.wasteMetricGreen
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Verify Now Button
            Button(
                onClick = onVerifyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.textPrimary,
                    contentColor = themeColors.background
                )
            ) {
                Text(
                    text = stringResource(R.string.verification_send_link_btn),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { dismissSheet() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.verification_later_btn),
                    fontFamily = MatterFontFamily,
                    color = themeColors.textSubtle,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationFlow(
    email: String,
    onSendEmail: () -> Unit,
    onCheckStatus: () -> Unit,
    isProcessing: Boolean,
    isSent: Boolean,
    isVerified: Boolean,
    onDismiss: () -> Unit
) {
    val themeColors = UniSwapTheme.colors

    AppBottomSheet(
        onDismissRequest = onDismiss,
        heightFraction = 0.85f,
        containerColor = themeColors.cardSurface,
        contentColor = themeColors.textPrimary
    ) {
        val dismissSheet = com.minimize.uniswap.ui.components.LocalBottomSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isVerified -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(themeColors.wasteMetricGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = themeColors.wasteMetricGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = stringResource(R.string.verification_success_title),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = themeColors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.verification_success_body),
                        fontFamily = MatterFontFamily,
                        fontSize = 13.sp,
                        color = themeColors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { dismissSheet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.textPrimary,
                            contentColor = themeColors.background
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.verification_continue_btn),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                !isSent -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(themeColors.btnBackBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MarkEmailUnread,
                            contentDescription = null,
                            tint = themeColors.textPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = stringResource(R.string.verification_sheet_title),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = themeColors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.verification_sent_body, email),
                        fontFamily = MatterFontFamily,
                        fontSize = 13.sp,
                        color = themeColors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onSendEmail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50.dp),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.textPrimary,
                            contentColor = themeColors.background
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = themeColors.background,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.verification_send_link_btn),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { dismissSheet() }) {
                        Text(
                            text = stringResource(R.string.verification_later_btn),
                            color = themeColors.textSubtle
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(themeColors.wasteMetricGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = themeColors.wasteMetricGreen,
                            modifier = Modifier.size(36.dp)
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
                        text = stringResource(R.string.verification_sent_body, email),
                        fontFamily = MatterFontFamily,
                        fontSize = 13.sp,
                        color = themeColors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onCheckStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50.dp),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.textPrimary,
                            contentColor = themeColors.background
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = themeColors.background,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.verification_check_status_btn),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { dismissSheet() }) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = themeColors.textSubtle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationBenefitRow(
    icon: ImageVector,
    title: String,
    body: String,
    tint: Color
) {
    val themeColors = UniSwapTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(themeColors.cardSurface)
            .border(0.75.dp, themeColors.divider, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
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
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = themeColors.textSecondary
            )
        }
    }
}
