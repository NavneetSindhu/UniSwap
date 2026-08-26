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

import com.minimize.uniswap.data.preferences.UserPreferencesManager

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: UserPreferencesManager
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
        combine(
            authRepository.getUserFlow(),
            preferencesManager.preferencesFlow
        ) { user, preferences ->
            val campus = user?.campusCenter?.ifBlank { null } ?: preferences.campusCenter.ifBlank { "" }
            _uiState.update { current ->
                current.copy(
                    userName = user?.displayName?.ifBlank { "Student User" } ?: "Student User",
                    userEmail = user?.email ?: "",
                    userPhotoUrl = user?.profilePicUrl?.ifBlank { null },
                    avatarId = user?.avatarId?.ifBlank { "avatar_scholar" } ?: "avatar_scholar",
                    campusCenter = campus,
                    isVerified = user?.isEmailVerified ?: false
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeMyItems() {
        if (myUserId.isBlank()) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        repository.getItemsBySellerFlow(myUserId)
            .onEach { myItems ->
                val rehomedCount = myItems.count { it.status == ItemStatus.SOLD }
                val kgDiverted = rehomedCount * 1.8 // 1.8 kg avg per university item
                val co2Offset = rehomedCount * 3.2 // 3.2 kg CO2 eq saved per circular item
                val tier = when {
                    rehomedCount >= 8 -> "champion"
                    rehomedCount >= 3 -> "contributor"
                    else -> "starter"
                }

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        kgSaved = kgDiverted,
                        co2Saved = co2Offset,
                        itemsRecycled = rehomedCount,
                        ecoTier = tier,
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
     * Updates the user's selected character avatar in Firestore
     */
    fun updateAvatar(avatarId: String) {
        viewModelScope.launch {
            authRepository.updateAvatar(avatarId)
        }
    }

    fun sendVerificationEmail(email: String = "", studentId: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingVerification = true) }
            val result = authRepository.sendVerificationEmail()
            if (result.isSuccess) {
                preferencesManager.updateStudentVerificationDetails(
                    collegeEmail = email.ifBlank { _uiState.value.userEmail },
                    studentId = studentId,
                    isPending = true,
                    sentTimestamp = System.currentTimeMillis()
                )
            }
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

    /**
     * UI State for the Profile Screen
     */
    data class ProfileUiState(
        val userName: String = "Student User",
        val userEmail: String = "",
        val userPhotoUrl: String? = null,
        val avatarId: String = "avatar_scholar",
        val campusCenter: String = "",
        val gradYear: String = "",
        val isVerified: Boolean = false,
        val isVerificationSent: Boolean = false,
        val isProcessingVerification: Boolean = false,
        val kgSaved: Double = 0.0,
        val co2Saved: Double = 0.0,
        val itemsRecycled: Int = 0,
        val ecoTier: String = "starter",
        val sellingItems: List<CampusItem> = emptyList(),
        val givenAwayItems: List<CampusItem> = emptyList(),
        val savedItems: List<CampusItem> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
