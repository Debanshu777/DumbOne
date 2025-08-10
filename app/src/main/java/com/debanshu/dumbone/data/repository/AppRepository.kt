package com.debanshu.dumbone.data.repository

import com.debanshu.dumbone.data.model.AppCategory
import com.debanshu.dumbone.data.model.AppCategoryInfo
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.AppUsageStats
import com.debanshu.dumbone.data.model.DailyUsageSummary
import com.debanshu.dumbone.data.model.HourlyUsage
import com.debanshu.dumbone.data.model.UserPreferences

interface AppRepository {
    // Existing methods
    suspend fun getInstalledApps(): List<AppInfo>
    suspend fun getEssentialApps(): List<AppInfo>
    suspend fun getLimitedApps(): List<AppInfo>
    suspend fun updateAppCategories(preferences: UserPreferences)
    suspend fun isAppInCooldown(packageName: String): Boolean
    suspend fun recordAppUsage(packageName: String)
    suspend fun getAppUsageStats(): List<AppUsageStats>
    suspend fun resetCooldownTimers()
    fun hasUsageStatsPermission(): Boolean
    fun requestUsageStatsPermission()

    // New methods for enhanced statistics with forceRealsData flag
    suspend fun getDailyUsageSummaries(daysToFetch: Int, forceRealData: Boolean = false): Map<Long, DailyUsageSummary>
    suspend fun getHourlyUsageForDay(timestamp: Long, forceRealData: Boolean = false): List<HourlyUsage>
    suspend fun getAppProductivityCategories(): List<AppCategoryInfo>
    suspend fun calculateProductivityScore(timestamp: Long, forceRealData: Boolean = false): Float
    suspend fun getTotalScreenTime(timestamp: Long, forceRealData: Boolean = false): Long
    suspend fun getAppOpenCount(timestamp: Long, forceRealData: Boolean = false): Int
    suspend fun getNotificationCount(timestamp: Long, forceRealData: Boolean = false): Int
    suspend fun getScreenUnlockCount(timestamp: Long, forceRealData: Boolean = false): Int
    suspend fun getAppUsageSummary(timestamp: Long, forceRealData: Boolean = false): List<AppUsageSummary>

    // Helper class for app usage summary
    data class AppUsageSummary(
        val packageName: String,
        val appName: String,
        val usageTime: Long,
        val openCount: Int,
        val isProductiveApp: Boolean,
        val category: AppCategory
    )
}