package com.minimize.uniswap.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
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
 * Bottom Sheet displaying thread-level actions (Delete Conversation, Block, Report).
 * Triggered on long-press or 3-dot tap in MessagesScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationActionBottomSheet(
    onDismissRequest: () -> Unit,
    displayName: String,
    itemTitle: String,
    onDeleteConversationClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit,
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
            // Header: Participant & Item Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = UniSwapTheme.colors.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (itemTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Regarding ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = UniSwapTheme.colors.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                color = UniSwapTheme.colors.divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Action: Delete Conversation
            ActionSheetRow(
                icon = Icons.Outlined.Delete,
                title = stringResource(R.string.action_delete_conversation),
                iconTint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error,
                onClick = onDeleteConversationClick
            )

            // Action: Report User
            ActionSheetRow(
                icon = Icons.Outlined.Flag,
                title = stringResource(R.string.action_report_user),
                onClick = onReportClick
            )

            // Action: Block User
            ActionSheetRow(
                icon = Icons.Outlined.Block,
                title = stringResource(R.string.action_block_seller),
                iconTint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error,
                onClick = onBlockClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
