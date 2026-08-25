package com.minimize.uniswap.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.MessageStatus
import com.minimize.uniswap.ui.theme.ErrorRed
import com.minimize.uniswap.ui.theme.ReadReceiptDelivered
import com.minimize.uniswap.ui.theme.ReadReceiptRead
import com.minimize.uniswap.ui.theme.ReadReceiptSent

/**
 * Renders WhatsApp/Telegram style tick-based read receipts:
 * - Single Gray Tick: SENT (Uploaded)
 * - Double Green Tick: DELIVERED (Received by counterparty)
 * - Double Blue Tick: READ (Opened & viewed by counterparty)
 * - Error Icon: FAILED
 */
@Composable
fun MessageReceiptStatus(
    status: MessageStatus,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp
) {
    when (status) {
        MessageStatus.SENDING -> {
            Icon(
                painter = painterResource(id = R.drawable.ic_tick_single),
                contentDescription = "Sending",
                tint = ReadReceiptSent.copy(alpha = 0.4f),
                modifier = modifier.size(size)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                painter = painterResource(id = R.drawable.ic_tick_single),
                contentDescription = "Sent",
                tint = ReadReceiptSent,
                modifier = modifier.size(size)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                painter = painterResource(id = R.drawable.ic_tick_double),
                contentDescription = "Delivered",
                tint = ReadReceiptDelivered,
                modifier = modifier.size(size + 2.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                painter = painterResource(id = R.drawable.ic_tick_double),
                contentDescription = "Read",
                tint = ReadReceiptRead,
                modifier = modifier.size(size + 2.dp)
            )
        }
        MessageStatus.FAILED -> {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Failed",
                tint = ErrorRed,
                modifier = modifier.size(size)
            )
        }
    }
}
