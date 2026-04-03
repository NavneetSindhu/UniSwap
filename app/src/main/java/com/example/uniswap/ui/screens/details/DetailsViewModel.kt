package com.example.uniswap.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.CampusItem
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.NetworkItemRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Simple UI State holder to manage Loading, Data, and Errors
data class DetailsUiState(
    val item: CampusItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DetailsViewModel(
    // Defaulting to the real Network Repository now
    private val repository: ItemRepository = NetworkItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    /**
     * Fetches the item details from your Spring Boot backend.
     */
    fun getItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Fetch the latest list from the backend
                val allItems = repository.getItems()

                // Find the specific item matching the ID from navigation
                val foundItem = allItems.find { it.id == itemId }

                if (foundItem != null) {
                    _uiState.update {
                        it.copy(item = foundItem, isLoading = false, error = null)
                    }
                } else {
                    _uiState.update {
                        it.copy(item = null, isLoading = false, error = "Oops! Item not found.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Connection failed. Check your server!")
                }
            }
        }
    }
}