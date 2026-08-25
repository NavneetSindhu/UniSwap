package com.minimize.uniswap.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.ReportReason
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Bottom sheet modal for submitting safety and UGC moderation reports.
 * Reuses [AppBottomSheet] for consistent styling and insets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmitReport: (reason: ReportReason, details: String) -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false
) {
    var selectedReason by remember { mutableStateOf(ReportReason.INAPPROPRIATE_CONTENT) }
    var additionalDetails by remember { mutableStateOf("") }

    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        heightFraction = 0.80f,
        containerColor = UniSwapTheme.colors.cardBackground,
        contentColor = UniSwapTheme.colors.textPrimary,
        showCloseIcon = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.report_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = UniSwapTheme.colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.report_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = UniSwapTheme.colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reason Radio Options
            ReportReason.values().forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReason = reason }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedReason == reason),
                        onClick = { selectedReason = reason },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = UniSwapTheme.colors.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(reason.stringResId),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedReason == reason) FontWeight.SemiBold else FontWeight.Normal,
                            color = UniSwapTheme.colors.textPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Additional details text field
            OutlinedTextField(
                value = additionalDetails,
                onValueChange = { additionalDetails = it },
                label = { Text(stringResource(R.string.report_details_label)) },
                placeholder = { Text(stringResource(R.string.report_details_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = UniSwapTheme.colors.divider,
                    focusedContainerColor = UniSwapTheme.colors.inputBackground,
                    unfocusedContainerColor = UniSwapTheme.colors.inputBackground,
                    focusedTextColor = UniSwapTheme.colors.textPrimary,
                    unfocusedTextColor = UniSwapTheme.colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = UniSwapTheme.colors.textPrimary
                    )
                }

                Button(
                    onClick = {
                        onSubmitReport(selectedReason, additionalDetails.trim())
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.report_submit_button),
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
