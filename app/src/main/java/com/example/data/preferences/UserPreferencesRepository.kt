package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexora_preferences")

data class UserPreferences(
    val activeProfile: String = "BALANCED",
    val hapticsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val defaultPingHost: String = "1.1.1.1",
    val lastOptimizedTimestamp: Long = 0L,
    val inAppRefreshIntervalMs: Long = 4000L,
    val recentlyLaunchedPackages: List<String> = emptyList(),
    val favoritePackages: Set<String> = emptySet(),
    val userAddedPackages: Set<String> = emptySet()
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_PROFILE = stringPreferencesKey("active_profile")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val DEFAULT_PING_HOST = stringPreferencesKey("default_ping_host")
        val LAST_OPTIMIZED = longPreferencesKey("last_optimized_timestamp")
        val REFRESH_INTERVAL_MS = longPreferencesKey("in_app_refresh_interval_ms")
        val RECENTLY_LAUNCHED = stringPreferencesKey("recently_launched_apps_csv")
        val FAVORITE_PACKAGES = stringSetPreferencesKey("favorite_packages_set")
        val USER_ADDED_PACKAGES = stringSetPreferencesKey("user_added_packages_set")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val rawRecent = preferences[PreferencesKeys.RECENTLY_LAUNCHED] ?: ""
        val recentList = if (rawRecent.isNotBlank()) rawRecent.split(",").filter { it.isNotBlank() } else emptyList()

        UserPreferences(
            activeProfile = preferences[PreferencesKeys.ACTIVE_PROFILE] ?: "BALANCED",
            hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
            soundEffectsEnabled = preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] ?: true,
            animationsEnabled = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true,
            defaultPingHost = preferences[PreferencesKeys.DEFAULT_PING_HOST] ?: "1.1.1.1",
            lastOptimizedTimestamp = preferences[PreferencesKeys.LAST_OPTIMIZED] ?: 0L,
            inAppRefreshIntervalMs = preferences[PreferencesKeys.REFRESH_INTERVAL_MS] ?: 4000L,
            recentlyLaunchedPackages = recentList,
            favoritePackages = preferences[PreferencesKeys.FAVORITE_PACKAGES] ?: emptySet(),
            userAddedPackages = preferences[PreferencesKeys.USER_ADDED_PACKAGES] ?: emptySet()
        )
    }

    suspend fun setActiveProfile(profile: String) {
        val refreshInterval = when (profile) {
            "PERFORMANCE" -> 2000L
            "BATTERY_SAVER" -> 8000L
            else -> 4000L
        }
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_PROFILE] = profile
            preferences[PreferencesKeys.REFRESH_INTERVAL_MS] = refreshInterval
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDefaultPingHost(host: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PING_HOST] = host
        }
    }

    suspend fun setLastOptimized(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_OPTIMIZED] = timestamp
        }
    }

    suspend fun addRecentlyLaunched(packageName: String) {
        context.dataStore.edit { preferences ->
            val raw = preferences[PreferencesKeys.RECENTLY_LAUNCHED] ?: ""
            val current = raw.split(",").filter { it.isNotBlank() && it != packageName }.toMutableList()
            current.add(0, packageName)
            val trimmed = current.take(10).joinToString(",")
            preferences[PreferencesKeys.RECENTLY_LAUNCHED] = trimmed
        }
    }

    suspend fun toggleFavoritePackage(packageName: String, isFavorite: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAVORITE_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (isFavorite) current.add(packageName) else current.remove(packageName)
            preferences[PreferencesKeys.FAVORITE_PACKAGES] = current
        }
    }

    suspend fun resetAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
