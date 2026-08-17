package com.minimize.uniswap.data.repository

import android.util.Log
import com.minimize.uniswap.data.api.RetrofitClient
import com.minimize.uniswap.data.model.CampusItem
// Ensure these point to the correct folder where you saved them!
import java.lang.Exception
import javax.inject.Inject

/**
 * Real repository implementation that talks to your Spring Boot backend.
 */
class NetworkItemRepository @Inject constructor() : ItemRepository {

    // This calls the 'instance' we defined in RetrofitClient.kt
    private val api = RetrofitClient.instance

    /**
     * Fetches the list of items from the backend.
     * ApiService must return List<CampusItem> for this to stay green!
     */
    override suspend fun getItems(): List<CampusItem> {
        return try {
            val items = api.getItems()
            Log.d("NetworkRepo", "Successfully fetched ${items.size} items from Backend")
            items
        } catch (e: Exception) {
            Log.e("NetworkRepo", "Error fetching items: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Posts a new CampusItem to your backend.
     * Returns true if the server responds with 200/201 OK.
     */
    override suspend fun postItem(item: CampusItem): Boolean {
        return try {
            // This calls the @POST method in your ApiService
            val response = api.addItem(item)

            Log.d("NetworkRepo", "Successfully posted item: ${response.title} with ID: ${response.id}")
            true
        } catch (e: Exception) {
            Log.e("NetworkRepo", "Failed to post item to backend")
            Log.e("NetworkRepo", "Error message: ${e.localizedMessage}")

            // Troubleshooting: If this fails, check if your laptop IP is correct
            // and if Spring Boot is actually running!
            e.printStackTrace()
            false
        }
    }
}
