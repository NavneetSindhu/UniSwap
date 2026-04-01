package com.example.uniswap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.ItemStatus
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.MockItemRepository
import kotlinx.coroutines.flow.*

class ProfileViewModel(
    private val repository: ItemRepository = MockItemRepository
) : ViewModel() {

    private val myUserId = "me_123" // Constant for current user

    // Transform raw items into a structured UI State
    val uiState: StateFlow<ProfileUiState> = repository.getAllItems()
        .map { allItems ->
            val myItems = allItems.filter { it.sellerId == myUserId }

            ProfileUiState(
                lbsSaved = myItems.size * 2.5, // Logic: Avg 2.5 lbs per item
                itemsRecycled = myItems.count { it.status == ItemStatus.SOLD },
                sellingItems = myItems.filter { it.status == ItemStatus.AVAILABLE },
                givenAwayItems = myItems.filter { it.status == ItemStatus.SOLD && it.isFree },
                // For now, saved items is a subset of all items (Mock logic)
                savedItems = allItems.take(2)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )
}