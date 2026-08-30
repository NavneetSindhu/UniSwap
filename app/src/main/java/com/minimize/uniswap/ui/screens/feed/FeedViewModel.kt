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

import com.minimize.uniswap.ui.screens.feed.components.CampusScope
import com.minimize.uniswap.ui.screens.feed.components.FeedSortOption

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val categoryConfigRepository: CategoryConfigRepository,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    val currentUserId: String = authRepository.getCurrentUserId() ?: ""
    val isGuestMode: StateFlow<Boolean> = authRepository.isGuestMode

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

    // Scope & Filter State Holders
    private val _campusScope = MutableStateFlow(CampusScope.MY_CAMPUS)
    val campusScope = _campusScope.asStateFlow()

    private val _selectedSort = MutableStateFlow(FeedSortOption.NEWEST)
    val selectedSort = _selectedSort.asStateFlow()

    private val _selectedCondition = MutableStateFlow<String?>(null)
    val selectedCondition = _selectedCondition.asStateFlow()

    private val _priceRange = MutableStateFlow(0f..10000f)
    val priceRange = _priceRange.asStateFlow()

    private val _freeOnly = MutableStateFlow(false)
    val freeOnly = _freeOnly.asStateFlow()

    private val _verifiedOnly = MutableStateFlow(false)
    val verifiedOnly = _verifiedOnly.asStateFlow()

    val activeFilterCount: StateFlow<Int> = combine(
        _selectedSort,
        _selectedCondition,
        _priceRange,
        _freeOnly,
        _verifiedOnly,
        _campusScope
    ) { flows ->
        val sort = flows[0] as FeedSortOption
        val condition = flows[1] as String?
        @Suppress("UNCHECKED_CAST")
        val price = flows[2] as ClosedFloatingPointRange<Float>
        val free = flows[3] as Boolean
        val verified = flows[4] as Boolean
        val scope = flows[5] as CampusScope

        var count = 0
        if (sort != FeedSortOption.NEWEST) count++
        if (condition != null) count++
        if (price.start > 0f || price.endInclusive < 10000f) count++
        if (free) count++
        if (verified) count++
        if (scope != CampusScope.MY_CAMPUS) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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
        blockedUserIds,
        userProfile,
        _campusScope,
        _selectedSort,
        _selectedCondition,
        _priceRange,
        _freeOnly,
        _verifiedOnly
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val items = flows[0] as List<CampusItem>
        val query = flows[1] as String
        val categoryId = flows[2] as String
        val blockedIds = flows[3] as Set<String>
        val user = flows[4] as User?
        val scope = flows[5] as CampusScope
        val sort = flows[6] as FeedSortOption
        val condition = flows[7] as String?
        val price = flows[8] as ClosedFloatingPointRange<Float>
        val free = flows[9] as Boolean
        val verified = flows[10] as Boolean

        val userCampus = user?.campusCenter.orEmpty().trim()

        val filtered = items.filter { item ->
            // Safety filter: never show items from blocked sellers
            if (item.sellerId in blockedIds) return@filter false

            // Campus Scope filter
            val matchesCampus = if (scope == CampusScope.MY_CAMPUS && userCampus.isNotBlank()) {
                val itemLoc = item.location.trim()
                val itemCampus = item.campusCenter.trim()

                val hasSpecificCampus = itemCampus.isNotBlank() && !itemCampus.equals("Campus", ignoreCase = true)
                val hasSpecificLocation = itemLoc.isNotBlank() && !itemLoc.equals("Campus", ignoreCase = true)

                if (hasSpecificCampus) {
                    itemCampus.contains(userCampus, ignoreCase = true) ||
                            userCampus.contains(itemCampus, ignoreCase = true)
                } else if (hasSpecificLocation) {
                    itemLoc.contains(userCampus, ignoreCase = true) ||
                            userCampus.contains(itemLoc, ignoreCase = true)
                } else {
                    // Generic campus-wide listing
                    true
                }
            } else {
                true
            }
            if (!matchesCampus) return@filter false

            // Search query filter
            val matchesSearch = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            if (!matchesSearch) return@filter false

            // Category filter
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
            if (!matchesCategory) return@filter false

            // Condition filter
            val matchesCondition = if (condition != null) {
                item.condition.equals(condition, ignoreCase = true)
            } else {
                true
            }
            if (!matchesCondition) return@filter false

            // Price range filter
            val matchesPrice = if (price.endInclusive < 10000f) {
                item.price in price.start.toDouble()..price.endInclusive.toDouble()
            } else {
                item.price >= price.start.toDouble()
            }
            if (!matchesPrice) return@filter false

            // Free items only filter
            if (free && !item.isFree && item.price > 0.0) return@filter false

            // Verified student sellers only filter
            if (verified && !item.isVerified) return@filter false

            true
        }

        // Sorting engine
        when (sort) {
            FeedSortOption.NEWEST -> filtered.sortedByDescending { it.timestamp }
            FeedSortOption.TRENDING -> filtered.sortedByDescending { it.calculateTrendingScore() }
            FeedSortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.price }
            FeedSortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.price }
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

    fun setCampusScope(scope: CampusScope) {
        _campusScope.value = scope
    }

    fun setSortOption(sortOption: FeedSortOption) {
        _selectedSort.value = sortOption
    }

    fun setCondition(condition: String?) {
        _selectedCondition.value = condition
    }

    fun setPriceRange(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }

    fun setFreeOnly(enabled: Boolean) {
        _freeOnly.value = enabled
    }

    fun setVerifiedOnly(enabled: Boolean) {
        _verifiedOnly.value = enabled
    }

    fun resetAllFilters() {
        _selectedSort.value = FeedSortOption.NEWEST
        _selectedCondition.value = null
        _priceRange.value = 0f..10000f
        _freeOnly.value = false
        _verifiedOnly.value = false
        _campusScope.value = CampusScope.MY_CAMPUS
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
