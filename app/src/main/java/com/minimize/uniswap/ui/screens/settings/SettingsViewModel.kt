package com.minimize.uniswap.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.preferences.ThemeMode
import com.minimize.uniswap.data.preferences.TypographyStyle
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesManager.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val user: StateFlow<com.minimize.uniswap.data.model.User?> = authRepository.getUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.updateThemeMode(mode)
        }
    }

    fun onDynamicColorChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateDynamicColor(enabled)
        }
    }

    fun onTypographyStyleChanged(style: TypographyStyle) {
        viewModelScope.launch {
            preferencesManager.updateTypographyStyle(style)
        }
    }

    fun onCampusCenterChanged(campus: String) {
        viewModelScope.launch {
            preferencesManager.updateCampusCenter(campus)
        }
    }

    fun onPushNotificationsChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updatePushNotifications(enabled)
        }
    }

    fun onEmailDigestChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateEmailDigest(enabled)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
