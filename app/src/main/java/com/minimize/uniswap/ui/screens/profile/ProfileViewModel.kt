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

import timber.log.Timber

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val myUserId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
        observeMyItems()
        observeSavedItems()
    }

    private fun observeUserProfile() {
        authRepository.getUserFlow()
            .onEach { user ->
                _uiState.update { current ->
                    current.copy(
                        userName = user?.displayName?.ifBlank { "Student User" } ?: "Student User",
                        userEmail = user?.email ?: "",
                        userPhotoUrl = user?.profilePicUrl?.ifBlank { null },
                        isVerified = user?.isEmailVerified ?: false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMyItems() {
        if (myUserId.isBlank()) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        repository.getItemsBySellerFlow(myUserId)
            .onEach { myItems ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        lbsSaved = myItems.size * 2.5,
                        itemsRecycled = myItems.count { it.status == ItemStatus.SOLD },
                        sellingItems = myItems.filter { it.status == ItemStatus.AVAILABLE },
                        givenAwayItems = myItems.filter { it.status == ItemStatus.SOLD && it.price == 0.0 },
                        error = null
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = "Could not load items.") }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSavedItems() {
        repository.getSavedItemsFlow()
            .onEach { savedList ->
                _uiState.update { it.copy(savedItems = savedList) }
            }
            .catch { e ->
                Timber.e(e, "Error observing saved items in ProfileViewModel")
            }
            .launchIn(viewModelScope)
    }

    fun fetchProfileData() {
        // Handled reactively by observers
    }

    /**
     * Toggles item status between AVAILABLE and SOLD in Firestore
     */
    fun toggleItemSoldStatus(item: CampusItem) {
        viewModelScope.launch {
            val newStatus = if (item.status == ItemStatus.AVAILABLE) ItemStatus.SOLD else ItemStatus.AVAILABLE
            repository.updateItemStatus(item.id, newStatus)
        }
    }

    /**
     * Permanently deletes an item listing from Firestore
     */
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    /**
     * Removes an item from saved items in Firestore
     */
    fun removeSavedItem(itemId: String) {
        viewModelScope.launch {
            repository.toggleSaveItem(itemId)
        }
    }

    /**
     * UI State for the Profile Screen
     */
    data class ProfileUiState(
        val userName: String = "Student User",
        val userEmail: String = "",
        val userPhotoUrl: String? = null,
        val isVerified: Boolean = false,
        val lbsSaved: Double = 0.0,
        val itemsRecycled: Int = 0,
        val sellingItems: List<CampusItem> = emptyList(),
        val givenAwayItems: List<CampusItem> = emptyList(),
        val savedItems: List<CampusItem> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
