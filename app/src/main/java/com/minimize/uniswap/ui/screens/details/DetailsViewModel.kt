package com.minimize.uniswap.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.data.model.Report
import com.minimize.uniswap.data.model.ReportReason
import com.minimize.uniswap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class DetailsUiState(
    val item: CampusItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String = "",
    val isGuestMode: Boolean = false,
    val isEmailVerified: Boolean = false,
    val userEmail: String = "",
    val showNudge: Boolean = false,
    val showVerificationFlow: Boolean = false,
    val isVerificationSent: Boolean = false,
    val isProcessingVerification: Boolean = false,
    val isSubmittingReport: Boolean = false,
    val isBlockingSeller: Boolean = false,
    val isSaved: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var savedJob: Job? = null

    init {
        val currentUid = authRepository.getCurrentUserId() ?: ""
        _uiState.update { it.copy(currentUserId = currentUid) }

        authRepository.isGuestMode
            .onEach { isGuest ->
                _uiState.update { it.copy(isGuestMode = isGuest) }
            }
            .launchIn(viewModelScope)
        
        // Observe User Profile for verification status
        authRepository.getUserFlow()
            .onEach { user ->
                _uiState.update { 
                    it.copy(
                        isEmailVerified = user?.isEmailVerified ?: false,
                        userEmail = user?.email ?: ""
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onClaimAttempt(onSuccess: () -> Unit) {
        // Email verification requirement is disabled for now
        onSuccess()
    }

    fun dismissNudge() {
        _uiState.update { it.copy(showNudge = false, showVerificationFlow = false) }
    }

    fun startVerificationFlow() {
        _uiState.update { it.copy(showNudge = false, showVerificationFlow = true) }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingVerification = true) }
            val result = authRepository.sendVerificationEmail()
            _uiState.update { 
                it.copy(
                    isProcessingVerification = false,
                    isVerificationSent = result.isSuccess
                )
            }
        }
    }

    fun checkVerificationStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingVerification = true) }
            authRepository.reloadUser()
            _uiState.update { it.copy(isProcessingVerification = false) }
        }
    }

    fun getItem(itemId: String) {
        if (itemId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid item ID.") }
            return
        }

        observeJob?.cancel()
        savedJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }

        observeJob = viewModelScope.launch {
            repository.getItemByIdFlow(itemId)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.localizedMessage ?: "Error loading item.")
                    }
                }
                .collect { item ->
                    _uiState.update {
                        it.copy(
                            item = item,
                            isLoading = false,
                            error = if (item == null) "Item not found." else null
                        )
                    }
                }
        }

        savedJob = viewModelScope.launch {
            repository.getSavedItemIdsFlow()
                .collect { savedIds ->
                    _uiState.update { it.copy(isSaved = savedIds.contains(itemId)) }
                }
        }
    }

    fun toggleSaveItem() {
        val currentItem = _uiState.value.item ?: return
        viewModelScope.launch {
            val result = repository.toggleSaveItem(currentItem.id)
            if (result.isSuccess) {
                val isSaved = result.getOrNull() ?: false
                _uiState.update {
                    it.copy(
                        isSaved = isSaved,
                        userMessage = if (isSaved) "Item added to Saved list" else "Item removed from Saved list"
                    )
                }
            }
        }
    }

    fun markAsSold(onComplete: () -> Unit = {}) {
        val currentItem = _uiState.value.item ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val newStatus = if (currentItem.status == com.minimize.uniswap.data.model.ItemStatus.SOLD) {
                com.minimize.uniswap.data.model.ItemStatus.AVAILABLE
            } else {
                com.minimize.uniswap.data.model.ItemStatus.SOLD
            }
            repository.updateItemStatus(currentItem.id, newStatus)
            _uiState.update { it.copy(isLoading = false) }
            onComplete()
        }
    }

    fun deleteListing(onComplete: () -> Unit = {}) {
        val currentItem = _uiState.value.item ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.deleteItem(currentItem.id)
            _uiState.update { it.copy(isLoading = false) }
            onComplete()
        }
    }

    fun submitReport(reason: ReportReason, additionalDetails: String, onComplete: (Boolean) -> Unit) {
        val currentItem = _uiState.value.item ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReport = true) }
            val report = Report(
                reportedUserId = currentItem.sellerId,
                itemId = currentItem.id,
                itemTitle = currentItem.title,
                reason = reason,
                additionalDetails = additionalDetails
            )
            val result = reportRepository.submitReport(report)
            _uiState.update { 
                it.copy(
                    isSubmittingReport = false,
                    userMessage = if (result.isSuccess) "Report submitted successfully" else "Failed to submit report"
                )
            }
            onComplete(result.isSuccess)
        }
    }

    fun blockSeller(onComplete: (Boolean) -> Unit) {
        val currentItem = _uiState.value.item ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBlockingSeller = true) }
            val result = reportRepository.blockUser(currentItem.sellerId)
            _uiState.update { 
                it.copy(
                    isBlockingSeller = false,
                    userMessage = if (result.isSuccess) "Seller blocked" else "Failed to block seller"
                )
            }
            onComplete(result.isSuccess)
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
