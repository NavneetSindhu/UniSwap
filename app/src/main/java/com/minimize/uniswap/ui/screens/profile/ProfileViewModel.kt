package com.minimize.uniswap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemStatus
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val myUserId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchProfileData()
    }

    /**
     * Fetches all items from Spring Boot and filters them for the current user's profile.
     */
    fun fetchProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val allItems = repository.getItems()

                // Filter items where Navneet is the seller
                val myItems = allItems.filter { it.sellerId == myUserId }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lbsSaved = myItems.size * 2.5, // Logic: Avg 2.5 lbs of waste saved per item
                        itemsRecycled = myItems.count { item -> item.status == ItemStatus.SOLD },
                        sellingItems = myItems.filter { item -> item.status == ItemStatus.AVAILABLE },
                        givenAwayItems = myItems.filter { item -> item.status == ItemStatus.SOLD && item.price == 0.0 },
                        // Saved items logic (can be expanded later with a 'Favorites' table in your DB)
                        savedItems = allItems.take(2),
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Could not load stats. Is the server running?")
                }
            }
        }
    }
}

/**
 * UI State for the Profile Screen
 */
data class ProfileUiState(
    val lbsSaved: Double = 0.0,
    val itemsRecycled: Int = 0,
    val sellingItems: List<CampusItem> = emptyList(),
    val givenAwayItems: List<CampusItem> = emptyList(),
    val savedItems: List<CampusItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
