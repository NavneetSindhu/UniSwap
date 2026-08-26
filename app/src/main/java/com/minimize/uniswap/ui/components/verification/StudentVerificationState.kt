package com.minimize.uniswap.ui.components.verification

/**
 * Steps in the Student Verification Bottom Sheet flow.
 */
enum class VerificationStep {
    /** Step 1: Input College Email & Student ID, overview of benefits */
    OVERVIEW,

    /** Step 2: Verification email dispatched; awaiting student inbox click & reload */
    PENDING_INBOX,

    /** Step 3: Verified student celebration state */
    SUCCESS
}

/**
 * State data representing active Student Verification workflow.
 */
data class StudentVerificationUiState(
    val currentStep: VerificationStep = VerificationStep.OVERVIEW,
    val collegeEmail: String = "",
    val studentId: String = "",
    val isSendingEmail: Boolean = false,
    val isCheckingStatus: Boolean = false,
    val isVerified: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val errorMessage: String? = null
)
