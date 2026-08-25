package com.minimize.uniswap.util

import com.minimize.uniswap.BuildConfig

/**
 * Centralized listing configuration rules.
 * Automatically adapts between Debug and Release build variants.
 */
object ListingConfig {

    /**
     * Minimum images required to publish a listing:
     * - Debug: 0 (allows rapid testing with default category placeholders)
     * - Release: 1 (students must upload at least 1 real photo)
     */
    val MIN_IMAGES_REQUIRED: Int
        get() = if (BuildConfig.DEBUG) 0 else 1

    /**
     * Maximum images allowed per listing:
     * - Debug: 50 (stress-testing carousels)
     * - Release: 5 (curated dorm showcase)
     */
    val MAX_IMAGES_ALLOWED: Int
        get() = if (BuildConfig.DEBUG) 50 else 5
}
