package com.minimize.uniswap.ui.screens.feed

import app.cash.turbine.test
import com.minimize.uniswap.data.model.*
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.CategoryConfigRepository
import com.minimize.uniswap.data.repository.ItemRepository
import com.minimize.uniswap.data.repository.ReportRepository
import com.minimize.uniswap.ui.screens.feed.components.CampusScope
import com.minimize.uniswap.ui.screens.feed.components.FeedSortOption
import com.minimize.uniswap.util.MainCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk(relaxed = true)
    private val categoryConfigRepository: CategoryConfigRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val reportRepository: ReportRepository = mockk(relaxed = true)

    private lateinit var viewModel: FeedViewModel

    private val sampleItems = listOf(
        CampusItem(
            id = "item_1",
            title = "Engineering Mathematics",
            description = "Higher Engineering Math by B.S. Grewal",
            price = 350.0,
            sellerId = "seller_1",
            sellerName = "Alice",
            category = ItemCategory.BOOKS,
            condition = "Good",
            campusCenter = "Main Campus",
            location = "Main Campus",
            status = ItemStatus.AVAILABLE
        ),
        CampusItem(
            id = "item_2",
            title = "Electric Kettle 1.5L",
            description = "Pigeon brand kettle for dorm",
            price = 0.0,
            sellerId = "seller_2",
            sellerName = "Bob (Blocked)",
            category = ItemCategory.DORM_ESSENTIALS,
            condition = "Like New",
            campusCenter = "Main Campus",
            location = "Main Campus",
            status = ItemStatus.AVAILABLE
        ),
        CampusItem(
            id = "item_3",
            title = "Mountain Cycle",
            description = "21 gear bicycle in great shape",
            price = 2500.0,
            sellerId = "seller_3",
            sellerName = "Charlie",
            category = ItemCategory.OTHER,
            condition = "Good",
            campusCenter = "North Campus",
            location = "North Campus",
            status = ItemStatus.AVAILABLE
        )
    )

    private val userFlow = MutableStateFlow<UserProfile?>(
        UserProfile(
            uid = "current_user",
            email = "me@campus.edu",
            displayName = "Current User",
            campusCenter = "Main Campus"
        )
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "current_user"
        every { authRepository.isGuestMode } returns MutableStateFlow(false)
        every { authRepository.getUserFlow() } returns userFlow
        every { reportRepository.getBlockedUserIdsFlow() } returns MutableStateFlow(setOf("seller_2"))
        every { itemRepository.getSavedItemIdsFlow() } returns MutableStateFlow(setOf("item_1"))
        every { itemRepository.getItemsFlow() } returns flowOf(sampleItems)
        every { categoryConfigRepository.categories } returns MutableStateFlow(emptyList())

        viewModel = FeedViewModel(
            repository = itemRepository,
            categoryConfigRepository = categoryConfigRepository,
            authRepository = authRepository,
            reportRepository = reportRepository
        )
    }

    @Test
    fun blockedUsers_areFilteredOutFromFeed() = runTest {
        viewModel.filteredItems.test {
            val items = awaitItem()
            // seller_2 should be excluded because they are in blockedUserIds
            assertTrue(items.none { it.sellerId == "seller_2" })
        }
    }

    @Test
    fun searchQuery_filtersItemsByTitleOrDescription() = runTest {
        viewModel.onSearchQueryChanged("Mathematics")

        viewModel.filteredItems.test {
            val items = awaitItem()
            assertTrue(items.all { it.title.contains("Mathematics", ignoreCase = true) })
            assertEquals(1, items.size)
            assertEquals("item_1", items.first().id)
        }
    }

    @Test
    fun campusScope_allCampuses_includesOtherCampuses() = runTest {
        viewModel.onCampusScopeChanged(CampusScope.ALL_CAMPUSES)

        viewModel.filteredItems.test {
            val items = awaitItem()
            // Should contain item_1 (Main Campus) and item_3 (North Campus), excluding blocked seller_2
            assertTrue(items.any { it.id == "item_3" })
        }
    }

    @Test
    fun freeOnlyFilter_showsOnlyFreeItems() = runTest {
        viewModel.onFreeOnlyChanged(true)

        viewModel.filteredItems.test {
            val items = awaitItem()
            assertTrue(items.all { it.price == 0.0 })
        }
    }

    @Test
    fun activeFilterCount_calculatesCorrectly() = runTest {
        viewModel.onSortChanged(FeedSortOption.PRICE_LOW_TO_HIGH)
        viewModel.onConditionSelected("Good")
        viewModel.onFreeOnlyChanged(true)

        viewModel.activeFilterCount.test {
            val count = awaitItem()
            assertEquals(3, count)
        }
    }

    @Test
    fun resetFilters_restoresDefaultState() = runTest {
        viewModel.onSortChanged(FeedSortOption.PRICE_HIGH_TO_LOW)
        viewModel.onConditionSelected("Fair")
        viewModel.onFreeOnlyChanged(true)
        viewModel.resetFilters()

        assertEquals(FeedSortOption.NEWEST, viewModel.selectedSort.value)
        assertNull(viewModel.selectedCondition.value)
        assertFalse(viewModel.freeOnly.value)
        assertEquals(CampusScope.MY_CAMPUS, viewModel.campusScope.value)
    }

    @Test
    fun toggleSaveItem_delegatesToRepository() = runTest {
        viewModel.toggleSaveItem("item_1")

        coVerify { itemRepository.toggleSaveItem("item_1") }
    }
}
