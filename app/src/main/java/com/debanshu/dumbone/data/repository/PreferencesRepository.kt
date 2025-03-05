package com.debanshu.dumbone.data.repository

import com.debanshu.dumbone.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun updatePreferences(userPreferences: UserPreferences)
    suspend fun updateEssentialApps(packageNames: List<String>)
    suspend fun updateLimitedApps(packageNames: List<String>)
    suspend fun setOnboardingCompleted(completed: Boolean)
}