package com.minimize.uniswap.ui.screens.list

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.util.CloudinaryHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ListUiState(
    val isEmailVerified: Boolean = false,
    val userEmail: String = "",
    val showNudge: Boolean = false,
    val showVerificationFlow: Boolean = false,
    val isVerificationSent: Boolean = false,
    val isProcessingVerification: Boolean = false,
    val uploadProgress: String? = null
)

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryHelper: CloudinaryHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ItemCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()

    private val _isPosting = MutableStateFlow(value = false)
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
    fun onDescriptionChange(newDescription: String) { _description.value = newDescription }
    fun onCategoryChange(category: ItemCategory) { _selectedCategory.value = category }

    fun onImagesSelected(uris: List<Uri>) {
        _selectedImages.value = (_selectedImages.value + uris).take(5)
    }

    fun onRemoveImage(index: Int) {
        if (index in _selectedImages.value.indices) {
            val updated = _selectedImages.value.toMutableList()
            updated.removeAt(index)
            _selectedImages.value = updated
        }
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
        // Email verification requirement is disabled for now
        postItem(onSuccess)
    }

    private fun postItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentPrice = _price.value.toDoubleOrNull() ?: 0.0
            if (_title.value.isBlank()) return@launch

            val userId = authRepository.getCurrentUserId() ?: return@launch
            _isPosting.value = true

            // Upload image to Cloudinary if user picked local photos
            var finalImageUrl = (_selectedCategory.value ?: ItemCategory.OTHER).getPlaceholderUrl()
            val imagesToUpload = _selectedImages.value

            if (imagesToUpload.isNotEmpty()) {
                val firstUri = imagesToUpload[0]
                val uploadResult = cloudinaryHelper.uploadImage(
                    context = context,
                    imageUri = firstUri,
                    folder = "uniswap/users/$userId/items",
                    tags = "user_$userId,uniswap_item"
                )
                uploadResult.onSuccess { uploadedUrl ->
                    finalImageUrl = uploadedUrl
                }.onFailure {
                    // Fallback to placeholder if upload fails or is not configured yet
                    finalImageUrl = firstUri.toString()
                }
            }

            val newItem = CampusItem(
                id = UUID.randomUUID().toString(),
                title = _title.value,
                description = _description.value.ifBlank { "No description provided." },
                price = currentPrice,
                category = _selectedCategory.value ?: ItemCategory.OTHER,
                location = "Campus",
                sellerId = userId,
                sellerName = "Campus User",
                timeAgo = "Just now",
                imageUrl = finalImageUrl,
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
