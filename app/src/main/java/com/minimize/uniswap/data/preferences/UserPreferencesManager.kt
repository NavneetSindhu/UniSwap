package com.minimize.uniswap.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val TYPOGRAPHY_STYLE = stringPreferencesKey("typography_style")
        val CAMPUS_CENTER = stringPreferencesKey("campus_center")
        val IS_VERIFIED = booleanPreferencesKey("is_verified")
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val EMAIL_DIGEST = booleanPreferencesKey("email_digest")
        val GUEST_MODE = booleanPreferencesKey("guest_mode")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeMode = ThemeMode.valueOf(
                preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            )
            val typographyStyle = TypographyStyle.valueOf(
                preferences[PreferencesKeys.TYPOGRAPHY_STYLE] ?: TypographyStyle.MODERN.name
            )

            UserPreferences(
                themeMode = themeMode,
                dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false,
                typographyStyle = typographyStyle,
                campusCenter = preferences[PreferencesKeys.CAMPUS_CENTER] ?: "Main Campus",
                isVerified = preferences[PreferencesKeys.IS_VERIFIED] ?: false,
                pushNotificationsEnabled = preferences[PreferencesKeys.PUSH_NOTIFICATIONS] ?: true,
                emailDigestEnabled = preferences[PreferencesKeys.EMAIL_DIGEST] ?: false
            )
        }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateTypographyStyle(style: TypographyStyle) {
        context.dataStore.edit { it[PreferencesKeys.TYPOGRAPHY_STYLE] = style.name }
    }

    suspend fun updateCampusCenter(campus: String) {
        context.dataStore.edit { it[PreferencesKeys.CAMPUS_CENTER] = campus }
    }

    suspend fun updateVerificationStatus(isVerified: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_VERIFIED] = isVerified }
    }

    suspend fun updatePushNotifications(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PUSH_NOTIFICATIONS] = enabled }
    }

    suspend fun updateEmailDigest(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.EMAIL_DIGEST] = enabled }
    }

    val isGuestModeFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.GUEST_MODE] ?: false
        }

    suspend fun updateGuestMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.GUEST_MODE] = enabled }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
