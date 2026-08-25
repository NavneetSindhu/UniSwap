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

import com.minimize.uniswap.util.ImageSanitizer
import com.minimize.uniswap.util.ListingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

data class ListUiState(
    val isEmailVerified: Boolean = false,
    val userEmail: String = "",
    val showNudge: Boolean = false,
    val showVerificationFlow: Boolean = false,
    val isVerificationSent: Boolean = false,
    val isProcessingVerification: Boolean = false,
    val isSanitizing: Boolean = false,
    val errorMessage: String? = null,
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

    fun onTitleChange(newTitle: String) { 
        _title.value = newTitle 
        if (_uiState.value.errorMessage != null) clearError()
    }
    fun onPriceChange(newPrice: String) { 
        _price.value = newPrice 
        if (_uiState.value.errorMessage != null) clearError()
    }
    fun onDescriptionChange(newDescription: String) { 
        _description.value = newDescription 
        if (_uiState.value.errorMessage != null) clearError()
    }
    fun onCategoryChange(category: ItemCategory) { 
        _selectedCategory.value = category 
        if (_uiState.value.errorMessage != null) clearError()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Sanitizes images immediately upon user selection on background IO dispatcher:
     * - Strips EXIF GPS coordinates & private device info
     * - Corrects orientation
     * - Downsamples & compresses to WebP cache files
     */
    fun onImagesSelected(uris: List<Uri>) {
        android.util.Log.i("ListViewModel", "onImagesSelected() received ${uris.size} URI(s): $uris")
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSanitizing = true, errorMessage = null) }
            val sanitizedUris = ImageSanitizer.sanitizeAll(context, uris)
            val updatedList = (_selectedImages.value + sanitizedUris).take(ListingConfig.MAX_IMAGES_ALLOWED)
            _selectedImages.value = updatedList
            _uiState.update { it.copy(isSanitizing = false) }
            android.util.Log.i("ListViewModel", "onImagesSelected() completed. Total selected: ${updatedList.size}")
        }
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
        if (_title.value.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an item title") }
            return
        }

        if (_selectedImages.value.size < ListingConfig.MIN_IMAGES_REQUIRED) {
            _uiState.update { it.copy(errorMessage = "Please add at least ${ListingConfig.MIN_IMAGES_REQUIRED} photo of your item") }
            return
        }

        postItem(onSuccess)
    }

    private fun postItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentPrice = _price.value.toDoubleOrNull() ?: 0.0
            if (_title.value.isBlank()) return@launch

            val userId = authRepository.getCurrentUserId() ?: return@launch
            _isPosting.value = true

            // Stream pre-sanitized, compressed WebP files to Cloudinary in parallel
            val imagesToUpload = _selectedImages.value
            val uploadedUrls = mutableListOf<String>()

            if (imagesToUpload.isNotEmpty()) {
                val uploadDeferreds = imagesToUpload.map { uri ->
                    async(Dispatchers.IO) {
                        cloudinaryHelper.uploadImage(
                            context = context,
                            imageUri = uri,
                            folder = "uniswap/users/$userId/items",
                            tags = "user_$userId,uniswap_item"
                        )
                    }
                }
                val results = uploadDeferreds.awaitAll()
                val moderationErrors = mutableListOf<String>()

                for (i in results.indices) {
                    results[i].onSuccess { url ->
                        uploadedUrls.add(url)
                    }.onFailure { error ->
                        val msg = error.message ?: "Upload failed"
                        if (msg.contains("flagged", ignoreCase = true) || msg.contains("inappropriate", ignoreCase = true)) {
                            moderationErrors.add(msg)
                        } else {
                            uploadedUrls.add(imagesToUpload[i].toString())
                        }
                    }
                }

                if (moderationErrors.isNotEmpty()) {
                    _isPosting.value = false
                    _uiState.update { it.copy(errorMessage = moderationErrors.first()) }
                    return@launch
                }
            }

            val finalImageUrl = uploadedUrls.firstOrNull() ?: (_selectedCategory.value ?: ItemCategory.OTHER).getPlaceholderUrl()
            val finalImageUrls = if (uploadedUrls.isNotEmpty()) uploadedUrls else listOf(finalImageUrl)

            val currentUser = authRepository.getCurrentUser()
            val sellerDisplayName = currentUser?.displayName?.ifBlank { "Campus User" } ?: "Campus User"

            val newItem = CampusItem(
                id = UUID.randomUUID().toString(),
                title = _title.value,
                description = _description.value.ifBlank { "No description provided." },
                price = currentPrice,
                category = _selectedCategory.value ?: ItemCategory.OTHER,
                location = "Campus",
                sellerId = userId,
                sellerName = sellerDisplayName,
                timeAgo = "Just now",
                imageUrl = finalImageUrl,
                imageUrls = finalImageUrls,
                isVerified = currentUser?.isEmailVerified ?: false,
                status = ItemStatus.AVAILABLE,
                timestamp = System.currentTimeMillis()
            )

            val success = repository.postItem(newItem)
            _isPosting.value = false

            if (success) {
                onSuccess()
            }
        }
    }
}
