package com.minimize.uniswap.data.model

import androidx.annotation.StringRes
import com.minimize.uniswap.R

/**
 * Predefined reasons for reporting user-generated content or users.
 */
enum class ReportReason(@StringRes val stringResId: Int) {
    INAPPROPRIATE_CONTENT(R.string.report_reason_inappropriate),
    SCAM_OR_FRAUD(R.string.report_reason_scam),
    PROHIBITED_ITEM(R.string.report_reason_prohibited),
    HARASSMENT_OR_ABUSE(R.string.report_reason_harassment),
    OTHER(R.string.report_reason_other)
}

/**
 * Status lifecycle of a moderation report.
 */
enum class ReportStatus {
    PENDING,
    UNDER_REVIEW,
    RESOLVED,
    DISMISSED
}

/**
 * Data class representing a safety report submitted by a campus user.
 */
data class Report(
    val id: String = "",
    val reporterId: String = "",
    val reportedUserId: String = "",
    val itemId: String? = null,
    val itemTitle: String? = null,
    val reason: ReportReason = ReportReason.OTHER,
    val additionalDetails: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.PENDING
) {
    /**
     * Converts to map for Firebase Firestore storage.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "reporterId" to reporterId,
        "reportedUserId" to reportedUserId,
        "itemId" to itemId,
        "itemTitle" to itemTitle,
        "reason" to reason.name,
        "additionalDetails" to additionalDetails,
        "timestamp" to timestamp,
        "status" to status.name
    )

    companion object {
        fun fromMap(map: Map<String, Any?>, documentId: String = ""): Report {
            val reasonStr = map["reason"] as? String ?: ReportReason.OTHER.name
            val statusStr = map["status"] as? String ?: ReportStatus.PENDING.name

            val parsedReason = try {
                ReportReason.valueOf(reasonStr)
            } catch (e: Exception) {
                ReportReason.OTHER
            }

            val parsedStatus = try {
                ReportStatus.valueOf(statusStr)
            } catch (e: Exception) {
                ReportStatus.PENDING
            }

            return Report(
                id = (map["id"] as? String)?.ifBlank { documentId } ?: documentId,
                reporterId = map["reporterId"] as? String ?: "",
                reportedUserId = map["reportedUserId"] as? String ?: "",
                itemId = map["itemId"] as? String,
                itemTitle = map["itemTitle"] as? String,
                reason = parsedReason,
                additionalDetails = map["additionalDetails"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                status = parsedStatus
            )
        }
    }
}
