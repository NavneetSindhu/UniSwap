package com.minimize.uniswap.ui.screens.feed

import app.cash.turbine.test
import com.minimize.uniswap.data.model.CampusItem
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.data.model.User
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

    private val sampleUser = User(
        uid = "user_feed_1",
        displayName = "Campus Student",
        campusCenter = "East Campus"
    )

    private val sampleItems = listOf(
        CampusItem(
            id = "item_1",
            title = "Physics Textbook",
            description = "Good condition for semester 2",
            price = 300.0,
            sellerId = "seller_1",
            sellerName = "Alice",
            category = ItemCategory.ENGINEERING,
            location = "East Campus Library",
            campusCenter = "East Campus"
        ),
        CampusItem(
            id = "item_2",
            title = "Desk Lamp",
            description = "LED table lamp",
            price = 0.0,
            isFree = true,
            sellerId = "seller_blocked",
            sellerName = "Blocked User",
            category = ItemCategory.DORM_ESSENTIALS,
            location = "East Campus Block A",
            campusCenter = "East Campus"
        ),
        CampusItem(
            id = "item_3",
            title = "Dorm Mattress",
            description = "Comfortable single bed mattress",
            price = 800.0,
            sellerId = "seller_2",
            sellerName = "Bob",
            category = ItemCategory.DORM_ESSENTIALS,
            location = "West Campus Hostel",
            campusCenter = "West Campus"
        ),
        CampusItem(
            id = "item_4",
            title = "Generic Notebook",
            description = "Campus wide item",
            price = 50.0,
            sellerId = "seller_3",
            sellerName = "Charlie",
            category = ItemCategory.ENGINEERING,
            location = "Campus",
            campusCenter = ""
        )
    )

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user_feed_1"
        every { authRepository.isGuestMode } returns MutableStateFlow(false)
        every { authRepository.getUserFlow() } returns flowOf(sampleUser)
        every { itemRepository.getItemsFlow() } returns flowOf(sampleItems)
        every { itemRepository.getSavedItemIdsFlow() } returns flowOf(emptySet())
        every { reportRepository.getBlockedUserIdsFlow() } returns MutableStateFlow(setOf("seller_blocked"))

        viewModel = FeedViewModel(
            repository = itemRepository,
            categoryConfigRepository = categoryConfigRepository,
            authRepository = authRepository,
            reportRepository = reportRepository
        )
    }

    @Test
    fun filteredItems_excludesBlockedSellers() = runTest {
        viewModel.setCampusScope(CampusScope.ALL_CAMPUSES)

        viewModel.filteredItems.test {
            val items = awaitItem()
            // item_2 is from seller_blocked, should be excluded
            assertTrue(items.none { it.sellerId == "seller_blocked" })
            assertEquals(3, items.size)
        }
    }

    @Test
    fun searchFiltering_filtersByQuery() = runTest {
        viewModel.setCampusScope(CampusScope.ALL_CAMPUSES)
        viewModel.updateSearchQuery("Physics")

        viewModel.filteredItems.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Physics Textbook", items.first().title)
        }
    }

    @Test
    fun campusScope_filtersByCampusAndIncludesGenericCampusItems() = runTest {
        viewModel.setCampusScope(CampusScope.MY_CAMPUS)

        viewModel.filteredItems.test {
            val items = awaitItem()
            // Should include East Campus items (item_1) and generic campus item (item_4), excluding blocked (item_2) and West Campus (item_3)
            assertEquals(2, items.size)
            assertTrue(items.any { it.id == "item_1" })
            assertTrue(items.any { it.id == "item_4" })
        }
    }

    @Test
    fun freeOnlyFilter_returnsOnlyFreeItems() = runTest {
        viewModel.setCampusScope(CampusScope.ALL_CAMPUSES)
        viewModel.setFreeOnly(true)

        viewModel.filteredItems.test {
            val items = awaitItem()
            // item_2 is free but blocked, so should be empty
            assertTrue(items.all { it.isFree || it.price == 0.0 })
        }
    }

    @Test
    fun activeFiltersCount_calculatesCorrectly() = runTest {
        viewModel.setSortOption(FeedSortOption.PRICE_LOW_TO_HIGH)
        viewModel.setCondition("Like New")
        viewModel.setFreeOnly(true)

        viewModel.activeFilterCount.test {
            val count = awaitItem()
            assertTrue(count >= 3)
        }
    }

    @Test
    fun resetAllFilters_clearsCustomFilters() = runTest {
        viewModel.setSortOption(FeedSortOption.PRICE_HIGH_TO_LOW)
        viewModel.setCondition("Fair")
        viewModel.setFreeOnly(true)

        viewModel.resetAllFilters()

        viewModel.activeFilterCount.test {
            val count = awaitItem()
            assertEquals(0, count)
        }
    }
}
