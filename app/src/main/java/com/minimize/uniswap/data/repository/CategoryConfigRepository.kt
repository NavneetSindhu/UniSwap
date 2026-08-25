package com.minimize.uniswap.data.repository

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minimize.uniswap.data.model.CampusCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryConfigRepository @Inject constructor() {

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val defaultCategories = listOf(
        CampusCategory("all", "All", "", 0),
        CampusCategory("books", "Books", "", 1),
        CampusCategory("cycles", "Cycles", "", 2),
        CampusCategory("electronics", "Electronics", "", 3),
        CampusCategory("clothing", "Clothing", "", 4),
        CampusCategory("dorm", "Dorm", "", 5)
    )

    private val _categories = MutableStateFlow<List<CampusCategory>>(defaultCategories)
    val categories: StateFlow<List<CampusCategory>> = _categories.asStateFlow()

    init {
        initializeRemoteConfig()
    }

    private fun initializeRemoteConfig() {
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600 // 1 hour in production
            }
            remoteConfig.setConfigSettingsAsync(configSettings)

            // 1. Set default in-app parameters
            val defaultJson = gson.toJson(defaultCategories)
            remoteConfig.setDefaultsAsync(mapOf(KEY_CATEGORIES to defaultJson))

            // 2. Fetch and activate on startup
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val categoriesJson = remoteConfig.getString(KEY_CATEGORIES)
                    parseAndEmit(categoriesJson)
                } else {
                    Timber.w(task.exception, "RemoteConfig fetch failed; using default categories.")
                }
            }

            // 3. Real-time Config Update Listener for instant cloud updates
            remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    Timber.d("Real-time RemoteConfig update received. Updated keys: %s", configUpdate.updatedKeys)
                    if (configUpdate.updatedKeys.contains(KEY_CATEGORIES)) {
                        remoteConfig.activate().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val updatedJson = remoteConfig.getString(KEY_CATEGORIES)
                                parseAndEmit(updatedJson)
                            }
                        }
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Timber.w(error, "RemoteConfig update listener error: %s", error.code)
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Error initializing RemoteConfig")
            _categories.value = defaultCategories
        }
    }

    private fun parseAndEmit(json: String) {
        scope.launch {
            try {
                if (json.isNotBlank()) {
                    val type = object : TypeToken<List<CampusCategory>>() {}.type
                    val parsed: List<CampusCategory> = gson.fromJson(json, type)
                    if (parsed.isNotEmpty()) {
                        _categories.value = parsed.sortedBy { it.order }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse categories JSON: %s", json)
            }
        }
    }

    companion object {
        private const val KEY_CATEGORIES = "campus_categories"
    }
}
