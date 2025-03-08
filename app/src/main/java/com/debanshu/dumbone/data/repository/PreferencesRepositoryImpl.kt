package com.debanshu.dumbone.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.debanshu.dumbone.data.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Extension property for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "min_launcher_preferences")

class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private object PreferencesKeys {
        val ESSENTIAL_APPS = stringPreferencesKey("essential_apps")
        val LIMITED_APPS = stringPreferencesKey("limited_apps")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
        val SHOW_USAGE_STATS = booleanPreferencesKey("show_usage_stats")
        val DAILY_RESET_TIME = intPreferencesKey("daily_reset_time")
    }

    override val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val essentialAppsString = preferences[PreferencesKeys.ESSENTIAL_APPS] ?: ""
            val limitedAppsString = preferences[PreferencesKeys.LIMITED_APPS] ?: ""

            UserPreferences(
                essentialApps = if (essentialAppsString.isNotEmpty())
                    essentialAppsString.split(",") else emptyList(),
                limitedApps = if (limitedAppsString.isNotEmpty())
                    limitedAppsString.split(",") else emptyList(),
                onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] == true,
                darkThemeEnabled = preferences[PreferencesKeys.DARK_THEME_ENABLED] == true,
                showUsageStats = preferences[PreferencesKeys.SHOW_USAGE_STATS] != false,
                dailyResetTime = preferences[PreferencesKeys.DAILY_RESET_TIME] ?: 0
            )
        }

    override suspend fun updatePreferences(userPreferences: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ESSENTIAL_APPS] = userPreferences.essentialApps.joinToString(",")
            preferences[PreferencesKeys.LIMITED_APPS] = userPreferences.limitedApps.joinToString(",")
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = userPreferences.onboardingCompleted
            preferences[PreferencesKeys.DARK_THEME_ENABLED] = userPreferences.darkThemeEnabled
            preferences[PreferencesKeys.SHOW_USAGE_STATS] = userPreferences.showUsageStats
            preferences[PreferencesKeys.DAILY_RESET_TIME] = userPreferences.dailyResetTime
        }
    }

    override suspend fun updateEssentialApps(packageNames: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ESSENTIAL_APPS] = packageNames.joinToString(",")
        }
    }

    override suspend fun updateLimitedApps(packageNames: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LIMITED_APPS] = packageNames.joinToString(",")
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}