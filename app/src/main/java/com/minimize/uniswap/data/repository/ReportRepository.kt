package com.minimize.uniswap.data.repository

import com.minimize.uniswap.data.model.Report
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface handling UGC moderation safety reports and user block lists.
 */
interface ReportRepository {

    /**
     * Submits a safety or moderation report to Firestore.
     */
    suspend fun submitReport(report: Report): Result<Unit>

    /**
     * Blocks a user by adding their userId to the current user's blocked list.
     */
    suspend fun blockUser(targetUserId: String): Result<Unit>

    /**
     * Unblocks a previously blocked user.
     */
    suspend fun unblockUser(targetUserId: String): Result<Unit>

    /**
     * Real-time stream of all user IDs blocked by the current signed-in user.
     */
    fun getBlockedUserIdsFlow(): Flow<Set<String>>
}
