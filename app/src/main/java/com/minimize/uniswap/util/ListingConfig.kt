package com.minimize.uniswap.util

/**
 * Centralized listing configuration rules.
 * Automatically adapts between Debug mode and Production mode (including Developer Prod simulation).
 */
object ListingConfig {

    /**
     * Minimum images required to publish a listing:
     * - Debug active: 0 (allows rapid testing with default category placeholders)
     * - Production: 1 (students must upload at least 1 real photo)
     */
    val MIN_IMAGES_REQUIRED: Int
        get() = if (DebugConfig.isDebug()) 0 else 1

    /**
     * Maximum images allowed per listing:
     * - Debug active: 50 (stress-testing carousels)
     * - Production: 5 (curated dorm showcase)
     */
    val MAX_IMAGES_ALLOWED: Int
        get() = if (DebugConfig.isDebug()) 50 else 5
}
