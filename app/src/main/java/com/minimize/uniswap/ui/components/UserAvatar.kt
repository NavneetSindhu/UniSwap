package com.minimize.uniswap.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minimize.uniswap.util.AvatarUtils

/**
 * Universal User Avatar composable for displaying character-based student avatars across UniSwap.
 */
@Composable
fun UserAvatar(
    avatarId: String?,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    contentDescription: String? = null
) {
    val drawableRes = AvatarUtils.getAvatarDrawable(avatarId)

    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = contentDescription ?: "User Avatar",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, CircleShape)
                else Modifier
            )
    )
}
