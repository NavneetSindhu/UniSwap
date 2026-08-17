package com.minimize.uniswap.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: ItemRepository
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
        // fetchItems() - We can remove the manual fetch and use the flow
        observeItems()
    }

    /**
     * Observes real-time updates from Firestore.
     */
    private fun observeItems() {
        repository.getItemsFlow()
            .onEach { result ->
                _rawItems.value = result
                _isRefreshing.value = false
            }
            .launchIn(viewModelScope)
    }

    /**
     * Reaches out to the backend to get the latest items manually.
     */
    fun fetchItems() {
        viewModelScope.launch {
            _isRefreshing.value = true
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
