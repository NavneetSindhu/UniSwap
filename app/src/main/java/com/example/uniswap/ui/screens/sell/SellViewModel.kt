package com.example.uniswap.ui.screens.sell

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.CampusItem
import com.example.uniswap.data.model.ItemCategory
import com.example.uniswap.data.model.ItemStatus
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.NetworkItemRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SellViewModel(
    // We initialize the real repository here
    private val repository: ItemRepository = NetworkItemRepository()
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ItemCategory.OTHER)
    val selectedCategory = _selectedCategory.asStateFlow()

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
        _selectedImages.value = uris.take(5)
    }

    fun performAIScan() {
        viewModelScope.launch {
            _isScanning.value = true
            delay(1500)
            _title.value = "Premium Engineering Drafter"
            _selectedCategory.value = ItemCategory.ENGINEERING
            _price.value = "25.0"
            _isScanning.value = false
        }
    }

    fun postItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentPrice = _price.value.toDoubleOrNull() ?: 0.0
            if (_title.value.isBlank()) return@launch

            _isPosting.value = true

            // Match exactly with your CampusItem data class parameters
            val newItem = CampusItem(
                id = UUID.randomUUID().toString(),
                title = _title.value,
                description = "Condition: New. Posted via Android.",
                price = currentPrice,
                category = _selectedCategory.value, // Pass the Enum object
                location = "Library Foyer",
                sellerId = "navneet_77", // Field required by your model
                sellerName = "Navneet Sindhu",
                timeAgo = "Just now",
                imageUrl = if (_selectedImages.value.isNotEmpty()) _selectedImages.value[0].toString() else "",
                isVerified = false,
                status = ItemStatus.AVAILABLE // Pass the Enum object
            )

            val success = repository.postItem(newItem)

            _isPosting.value = false

            if (success) {
                onSuccess()
            }
        }
    }
}