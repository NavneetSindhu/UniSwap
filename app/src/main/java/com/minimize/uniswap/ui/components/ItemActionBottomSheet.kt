package com.minimize.uniswap.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Share
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
 * Bottom Sheet displaying quick actions for an item or user (Share, Report, Block).
 * Triggered via 3-dot overflow in Details/Chat screens or long-press on product cards.
 * Reuses [AppBottomSheet] for consistent styling and insets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemActionBottomSheet(
    onDismissRequest: () -> Unit,
    itemTitle: String,
    sellerName: String,
    onShareClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSellerSelf: Boolean = false
) {
    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        heightFraction = null, // Wrap content height
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
            // Header: Item & Seller preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Text(
                    text = itemTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = UniSwapTheme.colors.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (sellerName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Listed by $sellerName",
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

            val dismissSheet = LocalBottomSheetDismiss.current

            // Action: Share
            ActionSheetRow(
                icon = Icons.Outlined.Share,
                title = stringResource(R.string.action_share_listing),
                onClick = {
                    dismissSheet()
                    onShareClick()
                }
            )

            // Actions for other users' listings (Report & Block)
            if (!isSellerSelf) {
                ActionSheetRow(
                    icon = Icons.Outlined.Flag,
                    title = stringResource(R.string.action_report_listing),
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = {
                        dismissSheet()
                        onReportClick()
                    }
                )

                ActionSheetRow(
                    icon = Icons.Outlined.Block,
                    title = stringResource(R.string.action_block_seller),
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = {
                        dismissSheet()
                        onBlockClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ActionSheetRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = UniSwapTheme.colors.textPrimary,
    textColor: Color = if (iconTint == MaterialTheme.colorScheme.error) iconTint else UniSwapTheme.colors.textPrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = textColor,
                fontSize = 15.sp
            )
        )
    }
}
