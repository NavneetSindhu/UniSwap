package com.minimize.uniswap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    /**
     * Exposes user preferences to the UI.
     * We use stateIn with SharingStarted.Eagerly to ensure it's loaded as soon as the VM is created.
     */
    val userPreferences: StateFlow<UserPreferences?> = preferencesManager.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null // null indicates "not loaded yet"
        )

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
