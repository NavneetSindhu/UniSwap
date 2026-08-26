package com.minimize.uniswap.ui.components.nudge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Contextual bottom sheet shown when a guest attempts a restricted action
 * such as Chat, Sell, Favorite, or accessing their Profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestNudgeBottomSheet(
    onDismissRequest: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.guest_nudge_title),
    subtitle: String = stringResource(R.string.guest_nudge_default_subtitle)
) {
    val themeColors = UniSwapTheme.colors

    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        heightFraction = null,
        containerColor = themeColors.cardSurface,
        contentColor = themeColors.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(themeColors.textPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = themeColors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title
            Text(
                text = title,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                color = themeColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contextual Subtitle
            Text(
                text = subtitle,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = themeColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            val dismissSheet = com.minimize.uniswap.ui.components.LocalBottomSheetDismiss.current

            // Primary Button: Sign In / Continue
            Button(
                onClick = {
                    dismissSheet()
                    onSignInClick()
                },
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
                    text = stringResource(R.string.guest_sign_in_email),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.28).sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Button: Create Student Account
            OutlinedButton(
                onClick = {
                    dismissSheet()
                    onSignUpClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeColors.textPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.guest_create_account),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
