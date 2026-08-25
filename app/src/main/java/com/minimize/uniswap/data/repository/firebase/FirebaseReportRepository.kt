package com.minimize.uniswap.data.repository.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.minimize.uniswap.data.model.Report
import com.minimize.uniswap.data.repository.ReportRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation of [ReportRepository].
 * Manages moderation report submissions and per-user block lists in Firestore.
 */
@Singleton
class FirebaseReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ReportRepository {

    override suspend fun submitReport(report: Report): Result<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be logged in to submit a report."))

        val finalReport = report.copy(
            id = report.id.ifBlank { UUID.randomUUID().toString() },
            reporterId = currentUserId,
            timestamp = System.currentTimeMillis()
        )

        return try {
            Timber.d("Submitting safety report: ID=%s, TargetUser=%s, Reason=%s", finalReport.id, finalReport.reportedUserId, finalReport.reason)
            firestore.collection("reports")
                .document(finalReport.id)
                .set(finalReport.toMap())
                .await()

            Timber.i("Report %s submitted successfully to Firestore.", finalReport.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to submit report %s to Firestore: %s", finalReport.id, e.message)
            Result.failure(e)
        }
    }

    override suspend fun blockUser(targetUserId: String): Result<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be logged in to block a user."))

        if (targetUserId.isBlank() || targetUserId == currentUserId) {
            return Result.failure(IllegalArgumentException("Cannot block yourself or an empty userId."))
        }

        return try {
            Timber.d("Blocking user %s by reporter %s", targetUserId, currentUserId)
            val blockData = mapOf(
                "blockedUserId" to targetUserId,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(currentUserId)
                .collection("blocked_users")
                .document(targetUserId)
                .set(blockData, SetOptions.merge())
                .await()

            Timber.i("User %s successfully blocked by %s.", targetUserId, currentUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to block user %s: %s", targetUserId, e.message)
            Result.failure(e)
        }
    }

    override suspend fun unblockUser(targetUserId: String): Result<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be logged in to unblock a user."))

        return try {
            Timber.d("Unblocking user %s", targetUserId)
            firestore.collection("users")
                .document(currentUserId)
                .collection("blocked_users")
                .document(targetUserId)
                .delete()
                .await()

            Timber.i("User %s successfully unblocked.", targetUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to unblock user %s: %s", targetUserId, e.message)
            Result.failure(e)
        }
    }

    override fun getBlockedUserIdsFlow(): Flow<Set<String>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            trySend(emptySet())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("users")
            .document(currentUserId)
            .collection("blocked_users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to blocked_users collection")
                    trySend(emptySet())
                    return@addSnapshotListener
                }

                val blockedIds = snapshot?.documents?.mapNotNull { it.id }?.toSet() ?: emptySet()
                trySend(blockedIds)
            }

        awaitClose { listenerRegistration.remove() }
    }
}
