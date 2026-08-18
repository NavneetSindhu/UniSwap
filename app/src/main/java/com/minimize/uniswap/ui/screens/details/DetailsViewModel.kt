package com.minimize.uniswap.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailsUiState(
    val item: CampusItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun getItem(itemId: String) {
        if (itemId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid item ID.") }
            return
        }

        observeJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }

        // 1. Observe from Room Flow (Single Source of Truth)
        observeJob = viewModelScope.launch {
            repository.getItemByIdFlow(itemId)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.localizedMessage ?: "Error loading item.")
                    }
                }
                .collect { cachedItem ->
                    if (cachedItem != null) {
                        _uiState.update {
                            it.copy(item = cachedItem, isLoading = false, error = null)
                        }
                    } else if (_uiState.value.item == null && !_uiState.value.isLoading) {
                        _uiState.update {
                            it.copy(error = "Item not found.")
                        }
                    }
                }
        }

        // 2. Trigger Firestore sync to update Room
        viewModelScope.launch {
            try {
                repository.fetchItemById(itemId)
            } catch (e: Exception) {
                if (_uiState.value.item == null) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Unable to fetch item from cloud.")
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}