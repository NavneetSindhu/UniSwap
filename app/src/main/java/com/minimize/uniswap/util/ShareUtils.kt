package com.minimize.uniswap.util

import android.content.Context
import android.content.Intent
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.CampusItem

/**
 * Centralized share utility for UniSwap.
 * Generates standardized item deep links and launches the platform share chooser.
 */
object ShareUtils {

    const val BASE_DEEP_LINK_URL = "https://uniswap.app/item"
    const val SCHEME_DEEP_LINK_URL = "uniswap://item"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.minimize.uniswap"

    /**
     * Builds the canonical universal deep link for a product.
     */
    fun getItemDeepLink(productId: String): String {
        return "$BASE_DEEP_LINK_URL/$productId"
    }

    /**
     * Centralized function to share a CampusItem across the app.
     */
    fun shareProduct(context: Context, item: CampusItem) {
        shareProduct(
            context = context,
            title = item.title,
            price = item.price,
            isFree = item.isFree,
            productId = item.id
        )
    }

    /**
     * Centralized function to share product metadata with standard deep link.
     */
    fun shareProduct(
        context: Context,
        title: String,
        price: Double,
        isFree: Boolean,
        productId: String
    ) {
        val link = getItemDeepLink(productId)
        val shareMessage = if (isFree || price == 0.0) {
            context.getString(R.string.share_item_free_template, title, link, PLAY_STORE_URL)
        } else {
            context.getString(R.string.share_item_template, title, price.toInt(), link, PLAY_STORE_URL)
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        val chooserTitle = context.getString(R.string.action_share_listing)
        context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
    }
}
