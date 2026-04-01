package com.example.uniswap.ui.screens.sell

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.CampusItem
import com.example.uniswap.data.model.ItemCategory
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.MockItemRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SellViewModel(
    private val repository: ItemRepository = MockItemRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow() // Fixed: Removed the _searchQuery reference

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ItemCategory.OTHER)
    val selectedCategory = _selectedCategory.asStateFlow()

    // Handle up to 5 images
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting = _isPosting.asStateFlow()

    fun onTitleChange(newTitle: String) { _title.value = newTitle }
    fun onPriceChange(newPrice: String) { _price.value = newPrice }
    fun onCategoryChange(category: ItemCategory) { _selectedCategory.value = category }

    fun onImagesSelected(uris: List<Uri>) {
        // Keep only the first 5
        _selectedImages.value = uris.take(5)
    }

    /**
     * Simulates the AI scanning the first uploaded photo to suggest details.
     */
    fun performAIScan() {
        viewModelScope.launch {
            _isScanning.value = true
            delay(1500) // "Thinking" time

            // "AI" filling the form
            _title.value = "Premium Engineering Drafter"
            _selectedCategory.value = ItemCategory.ENGINEERING
            _price.value = "25.0"

            _isScanning.value = false
        }
    }

    fun postItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isPosting.value = true

            val newItem = CampusItem(
                id = UUID.randomUUID().toString(), // Added dynamic ID generation
                title = _title.value,
                price = _price.value.toDoubleOrNull() ?: 0.0,
                category = _selectedCategory.value,
                location = "Library Foyer",
                sellerId = "me_123",
                sellerName = "Navneet S.",
                timeAgo = "Just now",
                // Use the first image as the thumbnail for the feed
                imageUrl = if (_selectedImages.value.isNotEmpty()) _selectedImages.value[0].toString() else ""
            )

            repository.postItem(newItem)
            _isPosting.value = false
            onSuccess()
        }
    }
}