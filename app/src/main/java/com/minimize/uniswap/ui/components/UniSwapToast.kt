package com.minimize.uniswap.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Toast classification types supporting semantic badge accents and icons.
 */
enum class ToastType {
    Error,
    Success,
    Warning,
    Info
}

/**
 * Immutable payload describing a custom toast notification.
 */
data class ToastData(
    val id: Long = System.currentTimeMillis(),
    val title: String? = null,
    val message: String,
    val type: ToastType = ToastType.Error,
    val durationMs: Long = 3500L
)

/**
 * State holder for managing toast presentation throughout the application.
 */
@Stable
class UniSwapToastHostState {
    var currentToast by mutableStateOf<ToastData?>(null)
        private set

    fun showError(message: String, title: String? = null, durationMs: Long = 3500L) {
        currentToast = ToastData(message = message, title = title, type = ToastType.Error, durationMs = durationMs)
    }

    fun showSuccess(message: String, title: String? = null, durationMs: Long = 3500L) {
        currentToast = ToastData(message = message, title = title, type = ToastType.Success, durationMs = durationMs)
    }

    fun showWarning(message: String, title: String? = null, durationMs: Long = 3500L) {
        currentToast = ToastData(message = message, title = title, type = ToastType.Warning, durationMs = durationMs)
    }

    fun showInfo(message: String, title: String? = null, durationMs: Long = 3500L) {
        currentToast = ToastData(message = message, title = title, type = ToastType.Info, durationMs = durationMs)
    }

    fun dismiss() {
        currentToast = null
    }
}

/**
 * CompositionLocal providing access to the global UniSwapToastHostState.
 */
val LocalToastHostState = staticCompositionLocalOf<UniSwapToastHostState> {
    error("No UniSwapToastHostState provided in CompositionLocalProvider")
}

/**
 * Standalone UI card for the custom Floating Island Toast.
 */
@Composable
fun UniSwapToastCard(
    toast: ToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = UniSwapTheme.colors

    val (accentColor, icon) = when (toast.type) {
        ToastType.Error -> Pair(ErrorRed, Icons.Outlined.ErrorOutline)
        ToastType.Success -> Pair(SuccessGreen, Icons.Default.CheckCircle)
        ToastType.Warning -> Pair(CampusAmber, Icons.Outlined.WarningAmber)
        ToastType.Info -> Pair(ActionLinkBlue, Icons.Outlined.Info)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .border(
                width = 0.75.dp,
                color = themeColors.divider.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        color = themeColors.cardSurface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Semantic Icon Badge with tinted circular background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = toast.type.name,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Title & Message text block
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (!toast.title.isNullOrBlank()) {
                    Text(
                        text = toast.title,
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        color = themeColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = toast.message,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (toast.title.isNullOrBlank()) themeColors.textPrimary else themeColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Compact Dismiss Action
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = themeColors.textSubtle,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Top-level Host container managing drop-in animations, auto-dismiss timers, and insets.
 */
@Composable
fun UniSwapToastHost(
    hostState: UniSwapToastHostState,
    modifier: Modifier = Modifier
) {
    val currentToast = hostState.currentToast
    val haptic = LocalHapticFeedback.current

    var activeToast by remember { mutableStateOf<ToastData?>(null) }

    LaunchedEffect(currentToast) {
        if (currentToast != null) {
            activeToast = currentToast
            when (currentToast.type) {
                ToastType.Error -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                ToastType.Success -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                else -> Unit
            }
            delay(currentToast.durationMs)
            hostState.dismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = currentToast != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(240, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
        ) {
            activeToast?.let { toast ->
                UniSwapToastCard(
                    toast = toast,
                    onDismiss = { hostState.dismiss() }
                )
            }
        }
    }
}

// ==========================================
// Previews
// ==========================================

@Preview(name = "Toast - Error (Dark)", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun UniSwapToastErrorDarkPreview() {
    UniSwapTheme(themeMode = com.minimize.uniswap.data.preferences.ThemeMode.DARK) {
        Box(modifier = Modifier.padding(16.dp)) {
            UniSwapToastCard(
                toast = ToastData(
                    title = "Authentication Failed",
                    message = "Please check your campus email and password.",
                    type = ToastType.Error
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Toast - Success (Dark)", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun UniSwapToastSuccessDarkPreview() {
    UniSwapTheme(themeMode = com.minimize.uniswap.data.preferences.ThemeMode.DARK) {
        Box(modifier = Modifier.padding(16.dp)) {
            UniSwapToastCard(
                toast = ToastData(
                    title = "Item Listed Successfully!",
                    message = "Your engineering textbook is now visible to campus peers.",
                    type = ToastType.Success
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Toast - All Variants (Light)", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun UniSwapToastAllVariantsLightPreview() {
    UniSwapTheme(themeMode = com.minimize.uniswap.data.preferences.ThemeMode.LIGHT) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UniSwapToastCard(
                toast = ToastData(
                    title = "Invalid Credentials",
                    message = "Password must be at least 6 characters.",
                    type = ToastType.Error
                ),
                onDismiss = {}
            )

            UniSwapToastCard(
                toast = ToastData(
                    title = "Welcome Back!",
                    message = "Signed in as Navneet Sindhu (USAR GGSIPU).",
                    type = ToastType.Success
                ),
                onDismiss = {}
            )

            UniSwapToastCard(
                toast = ToastData(
                    title = "Unsaved Changes",
                    message = "Make sure to save your listing before exiting.",
                    type = ToastType.Warning
                ),
                onDismiss = {}
            )

            UniSwapToastCard(
                toast = ToastData(
                    title = "Campus Meetup",
                    message = "Seller suggested meeting at PEC Library Quad.",
                    type = ToastType.Info
                ),
                onDismiss = {}
            )
        }
    }
}
