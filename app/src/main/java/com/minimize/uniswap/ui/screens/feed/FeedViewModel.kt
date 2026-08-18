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

    private val _rawItems = MutableStateFlow<List<CampusItem>>(emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

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
        observeItems()
    }

    private fun observeItems() {
        _isRefreshing.value = true
        repository.getItemsFlow()
            .onEach { result ->
                _rawItems.value = result
                _isRefreshing.value = false
            }
            .catch { _isRefreshing.value = false }
            .launchIn(viewModelScope)
    }

    fun fetchItems() {
        // Manual refresh still uses the flow observation, but we can trigger a one-shot fetch if desired
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
