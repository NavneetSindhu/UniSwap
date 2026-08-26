package com.minimize.uniswap.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Bottom Sheet displaying actions for a specific message bubble (Copy, Edit, Delete).
 * Triggered on message long-press in PickupChatScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionBottomSheet(
    onDismissRequest: () -> Unit,
    messageText: String,
    isFromMe: Boolean,
    isDeleted: Boolean,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        heightFraction = null,
        containerColor = UniSwapTheme.colors.cardBackground,
        contentColor = UniSwapTheme.colors.textPrimary,
        showCloseIcon = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            // Header: Preview snippet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Text(
                    text = if (isDeleted) stringResource(R.string.message_deleted_placeholder) else messageText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = UniSwapTheme.colors.textPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                color = UniSwapTheme.colors.divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val dismissSheet = LocalBottomSheetDismiss.current

            // Action: Copy (only if not deleted)
            if (!isDeleted && messageText.isNotBlank()) {
                ActionSheetRow(
                    icon = Icons.Outlined.ContentCopy,
                    title = stringResource(R.string.action_copy_text),
                    onClick = {
                        dismissSheet()
                        onCopyClick()
                    }
                )
            }

            // Actions for sender's own messages
            if (isFromMe && !isDeleted) {
                // Action: Edit
                ActionSheetRow(
                    icon = Icons.Outlined.Edit,
                    title = stringResource(R.string.action_edit_message),
                    onClick = {
                        dismissSheet()
                        onEditClick()
                    }
                )

                // Action: Delete
                ActionSheetRow(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(R.string.action_delete_message),
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        dismissSheet()
                        onDeleteClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
