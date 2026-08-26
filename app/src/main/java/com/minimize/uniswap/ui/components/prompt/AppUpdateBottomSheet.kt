package com.minimize.uniswap.ui.components.prompt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RocketLaunch
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
import com.minimize.uniswap.ui.components.LocalBottomSheetDismiss
import com.minimize.uniswap.ui.theme.ActionLinkBlue
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Modern In-App Update Bottom Sheet.
 * Displays version highlights, "What's New" release notes, and update action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateBottomSheet(
    latestVersion: String = "1.2.0",
    isMandatory: Boolean = false,
    onDismissRequest: () -> Unit,
    onUpdateClick: () -> Unit = {}
) {
    val themeColors = UniSwapTheme.colors

    val releaseHighlights = listOf(
        stringResource(R.string.update_feature_1),
        stringResource(R.string.update_feature_2),
        stringResource(R.string.update_feature_3),
        stringResource(R.string.update_feature_4)
    )

    AppBottomSheet(
        onDismissRequest = { if (!isMandatory) onDismissRequest() },
        heightFraction = 0.82f,
        containerColor = themeColors.cardSurface,
        contentColor = themeColors.textPrimary
    ) {
        val dismissSheet = LocalBottomSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rocket Icon Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ActionLinkBlue.copy(alpha = 0.15f))
                    .border(1.dp, ActionLinkBlue.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.RocketLaunch,
                    contentDescription = null,
                    tint = ActionLinkBlue,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.update_sheet_title),
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = themeColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.update_sheet_subtitle, latestVersion),
                fontFamily = MatterFontFamily,
                fontSize = 13.sp,
                color = themeColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // What's New Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(themeColors.cardBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.update_whats_new_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = themeColors.textPrimary
                )

                releaseHighlights.forEach { highlight ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = themeColors.wasteMetricGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = highlight,
                            fontFamily = MatterFontFamily,
                            fontSize = 12.sp,
                            color = themeColors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Update Button
            Button(
                onClick = {
                    onUpdateClick()
                    dismissSheet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.textPrimary,
                    contentColor = themeColors.background
                )
            ) {
                Text(
                    text = stringResource(R.string.update_now_btn),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            if (!isMandatory) {
                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { dismissSheet() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.update_later_btn),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = themeColors.textSecondary
                    )
                }
            }
        }
    }
}
