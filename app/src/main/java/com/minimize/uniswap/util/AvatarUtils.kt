package com.minimize.uniswap.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.minimize.uniswap.R

/**
 * Curated catalogue of unique student avatars in UniSwap.
 */
data class AvatarItem(
    val id: String,
    @DrawableRes val drawableRes: Int,
    @StringRes val titleResId: Int,
    @StringRes val tagResId: Int
)

object AvatarUtils {
    const val DEFAULT_AVATAR_ID = "avatar_scholar"

    val ALL_AVATARS: List<AvatarItem> = listOf(
        AvatarItem(
            id = "avatar_scholar",
            drawableRes = R.drawable.avatar_scholar,
            titleResId = R.string.avatar_scholar_title,
            tagResId = R.string.avatar_scholar_tag
        ),
        AvatarItem(
            id = "avatar_coder",
            drawableRes = R.drawable.avatar_coder,
            titleResId = R.string.avatar_coder_title,
            tagResId = R.string.avatar_coder_tag
        ),
        AvatarItem(
            id = "avatar_creative",
            drawableRes = R.drawable.avatar_creative,
            titleResId = R.string.avatar_creative_title,
            tagResId = R.string.avatar_creative_tag
        ),
        AvatarItem(
            id = "avatar_athlete",
            drawableRes = R.drawable.avatar_athlete,
            titleResId = R.string.avatar_athlete_title,
            tagResId = R.string.avatar_athlete_tag
        ),
        AvatarItem(
            id = "avatar_eco_ranger",
            drawableRes = R.drawable.avatar_eco_ranger,
            titleResId = R.string.avatar_eco_ranger_title,
            tagResId = R.string.avatar_eco_ranger_tag
        ),
        AvatarItem(
            id = "avatar_bookworm",
            drawableRes = R.drawable.avatar_bookworm,
            titleResId = R.string.avatar_bookworm_title,
            tagResId = R.string.avatar_bookworm_tag
        ),
        AvatarItem(
            id = "avatar_gamer",
            drawableRes = R.drawable.avatar_gamer,
            titleResId = R.string.avatar_gamer_title,
            tagResId = R.string.avatar_gamer_tag
        ),
        AvatarItem(
            id = "avatar_nomad",
            drawableRes = R.drawable.avatar_nomad,
            titleResId = R.string.avatar_nomad_title,
            tagResId = R.string.avatar_nomad_tag
        )
    )

    @DrawableRes
    fun getAvatarDrawable(avatarId: String?): Int {
        if (avatarId.isNullOrBlank()) return R.drawable.avatar_scholar
        return ALL_AVATARS.firstOrNull { it.id == avatarId }?.drawableRes ?: R.drawable.avatar_scholar
    }

    fun getAvatarItem(avatarId: String?): AvatarItem {
        return ALL_AVATARS.firstOrNull { it.id == avatarId } ?: ALL_AVATARS.first()
    }
}
