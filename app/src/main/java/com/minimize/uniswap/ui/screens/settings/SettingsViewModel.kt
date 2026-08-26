package com.minimize.uniswap.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.uniswap.data.model.Report
import com.minimize.uniswap.data.model.User
import com.minimize.uniswap.data.preferences.ThemeMode
import com.minimize.uniswap.data.preferences.TypographyStyle
import com.minimize.uniswap.data.preferences.UserPreferences
import com.minimize.uniswap.data.preferences.UserPreferencesManager
import com.minimize.uniswap.data.repository.AuthRepository
import com.minimize.uniswap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesManager.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val user: StateFlow<User?> = authRepository.getUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val blockedUserIds: StateFlow<Set<String>> = reportRepository.getBlockedUserIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val myReports: StateFlow<List<Report>> = reportRepository.getMyReportsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

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
            authRepository.updateCampusCenter(campus)
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

    fun unblockUser(targetUserId: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = reportRepository.unblockUser(targetUserId)
            if (result.isSuccess) {
                _userFeedbackMessage.value = "User unblocked successfully."
                onResult(true)
            } else {
                _userFeedbackMessage.value = "Failed to unblock user."
                onResult(false)
            }
        }
    }

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isDeletingAccount.value = true
            val result = authRepository.deleteAccount()
            _isDeletingAccount.value = false
            if (result.isSuccess) {
                _userFeedbackMessage.value = "Your account has been deleted."
                onSuccess()
            } else {
                _userFeedbackMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete account."
            }
        }
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }
}
