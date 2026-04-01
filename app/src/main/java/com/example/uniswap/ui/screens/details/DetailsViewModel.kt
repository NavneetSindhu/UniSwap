package com.example.uniswap.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.MockItemRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repository: ItemRepository = MockItemRepository
) : ViewModel() {

    // Initial state set to loading = true so the user sees a spinner immediately
    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    /**
     * Fetches the item details based on the [itemId].
     * This logic is ready for the Spring Boot migration.
     */
    fun getItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulating network latency for a smoother UX
            delay(400)

            val foundItem = repository.getItemById(itemId)

            if (foundItem != null) {
                _uiState.update {
                    it.copy(item = foundItem, isLoading = false, error = null)
                }
            } else {
                _uiState.update {
                    it.copy(item = null, isLoading = false, error = "Oops! Item not found.")
                }
            }
        }
    }
}