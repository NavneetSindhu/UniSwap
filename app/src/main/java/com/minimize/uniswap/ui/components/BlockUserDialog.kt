package com.minimize.uniswap.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Confirmation dialog for blocking a user.
 */
@Composable
fun BlockUserDialog(
    userName: String,
    onConfirmBlock: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isBlocking: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { if (!isBlocking) onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.block_dialog_title, userName.ifBlank { "User" }),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = UniSwapTheme.colors.textPrimary
                )
            )
        },
        text = {
            Text(
                text = stringResource(R.string.block_dialog_message),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = UniSwapTheme.colors.textSecondary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmBlock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isBlocking
            ) {
                Text(
                    text = stringResource(R.string.action_block),
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isBlocking
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = UniSwapTheme.colors.textPrimary
                )
            }
        },
        containerColor = UniSwapTheme.colors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    )
}
