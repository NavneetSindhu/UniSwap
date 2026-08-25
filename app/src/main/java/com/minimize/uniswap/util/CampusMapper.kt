package com.minimize.uniswap.util

import android.content.Context
import com.minimize.uniswap.R

/**
 * Utility mapper that converts full/long campus hub names to clean, compact abbreviations
 * for concise display in user handles, item cards, and profile badges using string resources.
 */
object CampusMapper {

    /**
     * Map of Full Name String Resource ID to Abbreviation String Resource ID
     */
    private val resourceMap: List<Pair<Int, Int>> = listOf(
        R.string.campus_usar_ggsipu to R.string.campus_usar_abbr,
        R.string.campus_ggsipu to R.string.campus_ggsipu_abbr,
        R.string.campus_pu to R.string.campus_pu_abbr,
        R.string.campus_pec to R.string.campus_pec_abbr,
        R.string.campus_uiet to R.string.campus_uiet_abbr,
        R.string.campus_chitkara to R.string.campus_chitkara_abbr,
        R.string.campus_thapar to R.string.campus_thapar_abbr,
        R.string.campus_iit_ropar to R.string.campus_iit_ropar_abbr
    )

    /**
     * Resolves the abbreviation using Context and String Resources.
     */
    fun toAbbreviation(context: Context, fullCampusName: String?): String {
        if (fullCampusName.isNullOrBlank()) return ""
        val trimmed = fullCampusName.trim()

        // 1. Direct match by evaluating string resources
        for ((fullNameRes, abbrRes) in resourceMap) {
            val fullName = context.getString(fullNameRes)
            if (trimmed.equals(fullName, ignoreCase = true) || trimmed.contains(fullName, ignoreCase = true)) {
                return context.getString(abbrRes)
            }
        }

        // 2. Keyword check for well-known campuses
        if (trimmed.contains("USAR", ignoreCase = true)) return context.getString(R.string.campus_usar_abbr)
        if (trimmed.contains("GGSIPU", ignoreCase = true) || trimmed.contains("Indraprastha", ignoreCase = true)) return context.getString(R.string.campus_ggsipu_abbr)
        if (trimmed.contains("PEC", ignoreCase = true) || trimmed.contains("Panjab Engineering", ignoreCase = true)) return context.getString(R.string.campus_pec_abbr)
        if (trimmed.contains("UIET", ignoreCase = true)) return context.getString(R.string.campus_uiet_abbr)
        if (trimmed.contains("Panjab", ignoreCase = true) || trimmed.contains("PU", ignoreCase = true)) return context.getString(R.string.campus_pu_abbr)
        if (trimmed.contains("Chitkara", ignoreCase = true)) return context.getString(R.string.campus_chitkara_abbr)
        if (trimmed.contains("Thapar", ignoreCase = true) || trimmed.contains("TIET", ignoreCase = true)) return context.getString(R.string.campus_thapar_abbr)
        if (trimmed.contains("IIT Ropar", ignoreCase = true)) return context.getString(R.string.campus_iit_ropar_abbr)

        // 3. Fallback: extract inside parentheses e.g. "College of Engg (COE)" -> "COE"
        val regexInParens = "\\(([^)]+)\\)".toRegex()
        val match = regexInParens.find(trimmed)
        if (match != null) {
            val inside = match.groupValues[1].trim()
            if (inside.length in 2..8) {
                return inside
            }
        }

        // 4. Default fallback
        return if (trimmed.length > 20) trimmed.take(18) + "…" else trimmed
    }
}
