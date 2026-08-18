package com.minimize.uniswap.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.repository.AuthRepository
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
    val error: String? = null,
    val currentUserId: String = ""
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        _uiState.update { it.copy(currentUserId = authRepository.getCurrentUserId() ?: "") }
    }

    fun getItem(itemId: String) {
        if (itemId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid item ID.") }
            return
        }

        observeJob?.cancel()
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
    }
}
