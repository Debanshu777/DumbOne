package com.debanshu.dumbone.data.repository

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.debanshu.dumbone.data.local.AppUsageStatsDao
import com.debanshu.dumbone.data.local.getAllAppUsageStats
import com.debanshu.dumbone.data.local.getAppUsageStats
import com.debanshu.dumbone.data.local.insertAppUsageStats
import com.debanshu.dumbone.data.local.resetAllCooldownTimers
import com.debanshu.dumbone.data.local.updateAppUsageStats
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.AppUsageStats
import com.debanshu.dumbone.data.model.TimerCalculator
import com.debanshu.dumbone.data.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val appUsageStatsDao: AppUsageStatsDao
) : AppRepository {

    override suspend fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = packageManager.queryIntentActivities(intent, 0).map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and
                    android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

            AppInfo(
                packageName = packageName,
                appName = appName,
                icon = icon,
                isSystemApp = isSystemApp
            )
        }

        // Sort apps alphabetically by name
        return apps.sortedBy { it.appName }
    }

    override suspend fun getEssentialApps(): List<AppInfo> {
        val allApps = getInstalledApps()
        val preferences = preferencesRepository.userPreferences.map { it }.first()

        return allApps.filter { app ->
            preferences.essentialApps.contains(app.packageName)
        }.also { apps ->
            apps.forEach { it.isEssential = true }
        }
    }

    override suspend fun getLimitedApps(): List<AppInfo> {
        val allApps = getInstalledApps()
        val preferences = preferencesRepository.userPreferences.map { it }.first()

        return allApps.filter { app ->
            preferences.limitedApps.contains(app.packageName)
        }.also { apps ->
            apps.forEach { it.isLimitedAccess = true }
        }
    }

    override suspend fun updateAppCategories(preferences: UserPreferences) {
        preferencesRepository.updatePreferences(preferences)
    }

    override suspend fun isAppInCooldown(packageName: String): Boolean {
        val usageStats = appUsageStatsDao.getAppUsageStats(packageName) ?: return false
        return usageStats.currentCooldownExpiry?.let {
            it > System.currentTimeMillis()
        } ?: false
    }

    override suspend fun recordAppUsage(packageName: String) {
        val existingStats = appUsageStatsDao.getAppUsageStats(packageName)
        val currentTime = System.currentTimeMillis()

        if (existingStats == null) {
            // First usage of the app
            appUsageStatsDao.insertAppUsageStats(
                AppUsageStats(
                    packageName = packageName,
                    lastUsedTimestamp = currentTime,
                    usageCount = 1,
                    totalUsageDuration = 0,
                    currentCooldownExpiry = null
                )
            )
        } else {
            // Calculate new cooldown if app is limited
            val preferences = preferencesRepository.userPreferences.map { it }.first()
            val isLimitedApp = preferences.limitedApps.contains(packageName)

            val newCooldownExpiry = if (isLimitedApp) {
                val newUsageCount = existingStats.usageCount + 1
                val cooldownTime = TimerCalculator.calculateCooldownTime(newUsageCount)
                currentTime + cooldownTime
            } else {
                null
            }

            appUsageStatsDao.updateAppUsageStats(
                existingStats.copy(
                    lastUsedTimestamp = currentTime,
                    usageCount = existingStats.usageCount + 1,
                    currentCooldownExpiry = newCooldownExpiry
                )
            )
        }
    }

    override suspend fun getAppUsageStats(): List<AppUsageStats> {
        return appUsageStatsDao.getAllAppUsageStats()
    }

    override suspend fun resetCooldownTimers() {
        appUsageStatsDao.resetAllCooldownTimers()
    }
}