package com.minimize.uniswap.ui.components

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.UniSwapTheme
import com.minimize.uniswap.util.LottieAnimationWrapper

/**
 * Standardized, reusable Empty State View matching UniSwap design system.
 * Features a centered Lottie animation (with graceful fallback to a vector icon),
 * Matter typography title & subtitle, and an optional action CTA button.
 */
@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    @RawRes lottieRes: Int? = R.raw.anim_empty_feed,
    fallbackIcon: ImageVector? = null,
    ctaText: String? = null,
    onCtaClick: (() -> Unit)? = null,
    animationSize: Dp = 160.dp
) {
    val themeColors = UniSwapTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 16.dp, bottom = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Animation or Fallback Icon
        if (lottieRes != null && lottieRes != 0) {
            LottieAnimationWrapper(
                resId = lottieRes,
                modifier = Modifier.size(animationSize)
            )
        } else if (fallbackIcon != null) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(themeColors.cardSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = themeColors.textSecondary,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Title
        Text(
            text = title,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            color = themeColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Subtitle
        Text(
            text = subtitle,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = themeColors.textSecondary,
            textAlign = TextAlign.Center
        )

        // 4. Optional CTA Action Button
        if (!ctaText.isNullOrBlank() && onCtaClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCtaClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.btnBackBg,
                    contentColor = themeColors.textPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = ctaText,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}
