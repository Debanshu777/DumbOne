package com.debanshu.dumbone.data.repository

import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.AppUsageStats
import com.debanshu.dumbone.data.model.UserPreferences

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
    suspend fun getEssentialApps(): List<AppInfo>
    suspend fun getLimitedApps(): List<AppInfo>
    suspend fun updateAppCategories(preferences: UserPreferences)
    suspend fun isAppInCooldown(packageName: String): Boolean
    suspend fun recordAppUsage(packageName: String)
    suspend fun getAppUsageStats(): List<AppUsageStats>
    suspend fun resetCooldownTimers()
}