package com.example.uniswap.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.CampusItem
import com.example.uniswap.data.model.ItemCategory
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.MockItemRepository
import kotlinx.coroutines.flow.*

class FeedViewModel(
    // We point to the Singleton MockItemRepository object
    // so data is shared across all screens.
    private val repository: ItemRepository = MockItemRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ItemCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    /**
     * UI State: This combines the raw items from the repository with the
     * search query and category filters in real-time.
     */
    val filteredItems: StateFlow<List<CampusItem>> = combine(
        repository.getAllItems(),
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            // Filter logic: Check if title contains query AND matches category
            val matchesSearch = item.title.contains(query, ignoreCase = true)
            val matchesCategory = category == null || item.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        // WhileSubscribed(5000) keeps the stream alive for 5 seconds after
        // the user leaves the screen (e.g., during navigation) to avoid flickering.
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: ItemCategory?) {
        // Toggle logic: If user clicks the same category twice, it clears the filter.
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }
}