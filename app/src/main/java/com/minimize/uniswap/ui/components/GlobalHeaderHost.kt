package com.minimize.uniswap.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Internal UI presentation payload representing either a transient toast or persistent offline status.
 */
private sealed interface HeaderPayload {
    val title: String
    val subtitle: String
    val icon: ImageVector
    val accentColor: Color
    val isDismissible: Boolean

    data class Toast(val data: ToastData) : HeaderPayload {
        override val title: String = data.title ?: when (data.type) {
            ToastType.Error -> "Notice"
            ToastType.Success -> "Success"
            ToastType.Warning -> "Warning"
            ToastType.Info -> "Info"
        }
        override val subtitle: String = data.message
        override val icon: ImageVector = when (data.type) {
            ToastType.Error -> Icons.Outlined.ErrorOutline
            ToastType.Success -> Icons.Default.CheckCircle
            ToastType.Warning -> Icons.Outlined.WarningAmber
            ToastType.Info -> Icons.Outlined.Info
        }
        override val accentColor: Color = when (data.type) {
            ToastType.Error -> ErrorRed
            ToastType.Success -> SuccessGreen
            ToastType.Warning -> CampusAmber
            ToastType.Info -> ActionLinkBlue
        }
        override val isDismissible: Boolean = true
    }

    object Offline : HeaderPayload {
        override val title: String = "You're offline"
        override val subtitle: String = "Showing cached campus feed"
        override val icon: ImageVector = Icons.Outlined.CloudOff
        override val accentColor: Color = CampusAmber
        override val isDismissible: Boolean = false
    }
}

/**
 * Unified Global Header Host.
 * Replaces floating overlays with a solid 50dp bottom-rounded header that physically
 * drops down from the top notch using spring physics, pushing the active screen content down.
 * Supports touch swipe-up dismiss, ✕ close button, and in-place content morphing.
 */
@Composable
fun GlobalHeaderHost(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    toastHostState: UniSwapToastHostState = LocalToastHostState.current
) {
    val themeColors = UniSwapTheme.colors
    val haptic = LocalHapticFeedback.current
    val currentToast = toastHostState.currentToast

    // Cache active payload so exiting animations render smoothly
    var activePayload by remember { mutableStateOf<HeaderPayload?>(null) }

    val targetPayload: HeaderPayload? = when {
        currentToast != null -> HeaderPayload.Toast(currentToast)
        isOffline -> HeaderPayload.Offline
        else -> null
    }

    LaunchedEffect(currentToast?.id) {
        if (currentToast != null) {
            when (currentToast.type) {
                ToastType.Error, ToastType.Success -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                else -> Unit
            }
            delay(currentToast.durationMs)
            toastHostState.dismiss()
        }
    }

    LaunchedEffect(targetPayload) {
        if (targetPayload != null) {
            activePayload = targetPayload
        }
    }

    val isVisible = targetPayload != null

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)),
        modifier = modifier.fillMaxWidth()
    ) {
        val payloadToRender = targetPayload ?: activePayload

        if (payloadToRender != null) {
            Surface(
                color = themeColors.cardSurface,
                shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(payloadToRender.isDismissible) {
                        if (payloadToRender.isDismissible) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -12f) {
                                    toastHostState.dismiss()
                                }
                            }
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start = 24.dp, end = 20.dp, top = 8.dp, bottom = 18.dp)
                ) {
                    AnimatedContent(
                        targetState = payloadToRender,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.95f, animationSpec = tween(160))) togetherWith
                                    (fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing)) +
                                            scaleOut(targetScale = 0.95f, animationSpec = tween(120)))
                        },
                        label = "GlobalHeaderContent"
                    ) { payload ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Circular Semantic Icon Badge (38x38 matching Chat back button)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(payload.accentColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = payload.icon,
                                    contentDescription = payload.title,
                                    tint = payload.accentColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // 2. Title + Subtitle Text Block
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (payload) {
                                        is HeaderPayload.Offline -> stringResource(R.string.offline_banner_title)
                                        is HeaderPayload.Toast -> payload.title
                                    },
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = themeColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = when (payload) {
                                        is HeaderPayload.Offline -> stringResource(R.string.offline_banner_subtitle)
                                        is HeaderPayload.Toast -> payload.subtitle
                                    },
                                    fontFamily = MatterFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = if (payload is HeaderPayload.Offline) CampusAmber else themeColors.textSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 3. Right Action: ✕ Close Button for Toasts OR Amber Badge for Offline
                            if (payload.isDismissible) {
                                IconButton(
                                    onClick = { toastHostState.dismiss() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = themeColors.textSubtle,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(CampusAmber.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.offline_badge_tag),
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = CampusAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
