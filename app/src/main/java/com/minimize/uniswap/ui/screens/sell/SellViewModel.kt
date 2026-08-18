package com.minimize.uniswap.ui.screens.sell

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SellUiState(
    val isEmailVerified: Boolean = false,
    val userEmail: String = "",
    val showNudge: Boolean = false,
    val showVerificationFlow: Boolean = false,
    val isVerificationSent: Boolean = false,
    val isProcessingVerification: Boolean = false
)

@HiltViewModel
class SellViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellUiState())
    val uiState: StateFlow<SellUiState> = _uiState.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ItemCategory.OTHER)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting = _isPosting.asStateFlow()

    init {
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

    fun onTitleChange(newTitle: String) { _title.value = newTitle }
    fun onPriceChange(newPrice: String) { _price.value = newPrice }
    fun onCategoryChange(category: ItemCategory) { _selectedCategory.value = category }

    fun onImagesSelected(uris: List<Uri>) {
        _selectedImages.value = uris.take(5)
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

    fun onPostAttempt(onSuccess: () -> Unit) {
        if (_uiState.value.isEmailVerified) {
            postItem(onSuccess)
        } else {
            _uiState.update { it.copy(showNudge = true) }
        }
    }

    private fun postItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentPrice = _price.value.toDoubleOrNull() ?: 0.0
            if (_title.value.isBlank()) return@launch

            val userId = authRepository.getCurrentUserId() ?: return@launch
            _isPosting.value = true

            val newItem = CampusItem(
                id = UUID.randomUUID().toString(),
                title = _title.value,
                description = "Condition: New. Posted via Android.",
                price = currentPrice,
                category = _selectedCategory.value,
                location = "Library Foyer",
                sellerId = userId,
                sellerName = "Campus User",
                timeAgo = "Just now",
                imageUrl = if (_selectedImages.value.isNotEmpty()) _selectedImages.value[0].toString() else _selectedCategory.value.getPlaceholderUrl(),
                isVerified = false,
                status = ItemStatus.AVAILABLE
            )

            val success = repository.postItem(newItem)
            _isPosting.value = false

            if (success) {
                onSuccess()
            }
        }
    }
}
