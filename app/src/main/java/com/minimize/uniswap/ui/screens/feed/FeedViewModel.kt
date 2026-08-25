package com.minimize.uniswap.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.CampusCategory
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.CategoryConfigRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.data.model.Report
import com.minimize.uniswap.data.model.ReportReason
import com.minimize.uniswap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val categoryConfigRepository: CategoryConfigRepository,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    val currentUserId: String = authRepository.getCurrentUserId() ?: ""

    val userProfile: StateFlow<User?> = authRepository.getUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val blockedUserIds: StateFlow<Set<String>> = reportRepository.getBlockedUserIdsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val savedItemIds: StateFlow<Set<String>> = repository.getSavedItemIdsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    val categories: StateFlow<List<CampusCategory>> = categoryConfigRepository.categories

    private val _rawItems = MutableStateFlow<List<CampusItem>>(emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = combine(
        _isLoading,
        com.minimize.uniswap.util.DebugConfig.forceShimmerLoading
    ) { loading, forceShimmer ->
        loading || forceShimmer
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val filteredItems: StateFlow<List<CampusItem>> = combine(
        _rawItems,
        _searchQuery,
        _selectedCategoryId,
        blockedUserIds
    ) { items, query, categoryId, blockedIds ->
        items.filter { item ->
            // Safety filter: never show items from blocked sellers
            if (item.sellerId in blockedIds) return@filter false

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
        repository.getItemsFlow()
            .onEach { result ->
                _rawItems.value = result
                _isLoading.value = false
            }
            .catch { e ->
                timber.log.Timber.e(e, "Error observing items")
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun fetchItems() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = repository.getItems()
                _rawItems.value = result
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to refresh items")
            } finally {
                _isRefreshing.value = false
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    private val _isSubmittingReport = MutableStateFlow(false)
    val isSubmittingReport: StateFlow<Boolean> = _isSubmittingReport.asStateFlow()

    private val _isBlockingSeller = MutableStateFlow(false)
    val isBlockingSeller: StateFlow<Boolean> = _isBlockingSeller.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun submitReport(item: CampusItem, reason: ReportReason, details: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSubmittingReport.value = true
            val report = Report(
                reportedUserId = item.sellerId,
                itemId = item.id,
                itemTitle = item.title,
                reason = reason,
                additionalDetails = details
            )
            val result = reportRepository.submitReport(report)
            _isSubmittingReport.value = false
            _userMessage.value = if (result.isSuccess) "Report submitted successfully" else "Failed to submit report"
            onComplete(result.isSuccess)
        }
    }

    fun blockSeller(sellerId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isBlockingSeller.value = true
            val result = reportRepository.blockUser(sellerId)
            _isBlockingSeller.value = false
            _userMessage.value = if (result.isSuccess) "User blocked" else "Failed to block user"
            onComplete(result.isSuccess)
        }
    }

    fun toggleSaveItem(itemId: String) {
        viewModelScope.launch {
            val result = repository.toggleSaveItem(itemId)
            if (result.isSuccess) {
                val isSaved = result.getOrNull() ?: false
                _userMessage.value = if (isSaved) "Item added to Saved list" else "Item removed from Saved list"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
