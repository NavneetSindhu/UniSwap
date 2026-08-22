package com.minimize.uniswap.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusCategory
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.repository.CategoryConfigRepository
import com.minimize.uniswap.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val categoryConfigRepository: CategoryConfigRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    val categories: StateFlow<List<CampusCategory>> = categoryConfigRepository.categories

    private val _rawItems = MutableStateFlow<List<CampusItem>>(emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val filteredItems: StateFlow<List<CampusItem>> = combine(
        _rawItems,
        _searchQuery,
        _selectedCategoryId
    ) { items, query, categoryId ->
        items.filter { item ->
            val matchesSearch = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)

            val matchesCategory = if (categoryId == "all") {
                true
            } else {
                val catName = item.category.name
                val title = item.title
                when (categoryId.lowercase()) {
                    "books", "notes" -> catName.contains("ENGINEERING", ignoreCase = true) ||
                            title.contains("note", ignoreCase = true) ||
                            title.contains("book", ignoreCase = true) ||
                            title.contains("pdf", ignoreCase = true)
                    "cycles" -> title.contains("cycle", ignoreCase = true) ||
                            title.contains("bike", ignoreCase = true)
                    "electronics" -> item.category == ItemCategory.ELECTRONICS ||
                            title.contains("electronic", ignoreCase = true) ||
                            title.contains("tech", ignoreCase = true) ||
                            title.contains("laptop", ignoreCase = true)
                    "clothing" -> title.contains("cloth", ignoreCase = true) ||
                            title.contains("shoe", ignoreCase = true) ||
                            title.contains("wear", ignoreCase = true)
                    "dorm" -> item.category == ItemCategory.DORM_ESSENTIALS ||
                            title.contains("dorm", ignoreCase = true) ||
                            title.contains("kettle", ignoreCase = true)
                    else -> true
                }
            }
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

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }
}
