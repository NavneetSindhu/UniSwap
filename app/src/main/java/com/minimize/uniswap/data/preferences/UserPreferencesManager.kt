package com.minimize.uniswap.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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
        val COLLEGE_EMAIL = stringPreferencesKey("college_email")
        val STUDENT_ID = stringPreferencesKey("student_id")
        val IS_VERIFICATION_PENDING = booleanPreferencesKey("is_verification_pending")
        val VERIFICATION_SENT_TIMESTAMP = longPreferencesKey("verification_sent_timestamp")
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
                collegeEmail = preferences[PreferencesKeys.COLLEGE_EMAIL] ?: "",
                studentId = preferences[PreferencesKeys.STUDENT_ID] ?: "",
                isVerificationPending = preferences[PreferencesKeys.IS_VERIFICATION_PENDING] ?: false,
                verificationSentTimestamp = preferences[PreferencesKeys.VERIFICATION_SENT_TIMESTAMP] ?: 0L,
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
        context.dataStore.edit { 
            it[PreferencesKeys.IS_VERIFIED] = isVerified
            if (isVerified) {
                it[PreferencesKeys.IS_VERIFICATION_PENDING] = false
            }
        }
    }

    suspend fun updateStudentVerificationDetails(
        collegeEmail: String,
        studentId: String,
        isPending: Boolean,
        sentTimestamp: Long
    ) {
        context.dataStore.edit {
            it[PreferencesKeys.COLLEGE_EMAIL] = collegeEmail
            it[PreferencesKeys.STUDENT_ID] = studentId
            it[PreferencesKeys.IS_VERIFICATION_PENDING] = isPending
            it[PreferencesKeys.VERIFICATION_SENT_TIMESTAMP] = sentTimestamp
        }
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

    suspend fun getPromptLastShownTimestamp(promptKey: String): Long {
        val key = longPreferencesKey("prompt_last_shown_$promptKey")
        val prefs = context.dataStore.data.first()
        return prefs[key] ?: 0L
    }

    suspend fun updatePromptLastShownTimestamp(promptKey: String, timestamp: Long) {
        val key = longPreferencesKey("prompt_last_shown_$promptKey")
        context.dataStore.edit { it[key] = timestamp }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
