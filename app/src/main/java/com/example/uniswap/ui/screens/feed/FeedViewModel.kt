package com.example.uniswap.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniswap.data.model.CampusItem
import com.example.uniswap.data.model.ItemCategory
import com.example.uniswap.data.repository.ItemRepository
import com.example.uniswap.data.repository.NetworkItemRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FeedViewModel(
    // Default to our new Network Repository
    private val repository: ItemRepository = NetworkItemRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ItemCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    // Internal state to hold items fetched from the backend
    private val _rawItems = MutableStateFlow<List<CampusItem>>(emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * UI State: Combines the raw items from the backend with
     * search and category filters.
     */
    val filteredItems: StateFlow<List<CampusItem>> = combine(
        _rawItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesSearch = item.title.contains(query, ignoreCase = true)
            val matchesCategory = category == null || item.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        fetchItems()
    }

    /**
     * Reaches out to the Spring Boot backend to get the latest items.
     */
    fun fetchItems() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Calls api.getItems() through the repository
            val result = repository.getItems()
            _rawItems.value = result
            _isRefreshing.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: ItemCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }
}