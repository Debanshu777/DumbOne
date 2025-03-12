package com.debanshu.dumbone.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val appUsageStatsDao: AppUsageStatsDao
) : AppRepository {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    // Check if we have permission to access usage stats
    override fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Helper function to request permission
    override fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = packageManager.queryIntentActivities(intent, 0).map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and
                    android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

            // Extract dominant color from the app icon
            val dominantColor = extractDominantColor(icon)

            AppInfo(
                packageName = packageName,
                appName = appName,
                icon = icon,
                isSystemApp = isSystemApp,
                dominantColor = dominantColor
            )
        }

        // Sort apps alphabetically by name
        apps.sortedBy { it.appName }
    }

    // Helper function to extract dominant color from app icon
    private suspend fun extractDominantColor(drawable: Drawable?): Color = withContext(Dispatchers.Default) {
        if (drawable == null) return@withContext Color.Gray

        // Convert drawable to bitmap
        val bitmap = try {
            (drawable as? BitmapDrawable)?.bitmap ?: Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            ).also { bitmap ->
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        } catch (e: Exception) {
            return@withContext Color.Gray
        }

        // Extract color using Palette API
        return@withContext try {
            val palette = Palette.from(bitmap).generate()
            val defaultColor = 0x888888
            Color(palette.getDominantColor(defaultColor))
        } catch (e: Exception) {
            Color.Gray
        }
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

        // Update app usage duration from system
        updateAppUsageDurations()
    }

    // New method to update app usage durations from UsageStatsManager
    private suspend fun updateAppUsageDurations() {
        if (!hasUsageStatsPermission() || usageStatsManager == null) {
            return
        }

        try {
            // Query for the last 24 hours
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (24 * 60 * 60 * 1000) // 24 hours in milliseconds

            val usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)

            // Get all our tracked apps
            val currentStats = appUsageStatsDao.getAllAppUsageStats()

            // Update each app's usage duration
            for (stat in currentStats) {
                usageStatsMap[stat.packageName]?.let { usageStat ->
                    val totalTimeInForeground = usageStat.totalTimeInForeground

                    // Only update if the system has a higher usage time
                    if (totalTimeInForeground > stat.totalUsageDuration) {
                        appUsageStatsDao.updateAppUsageStats(
                            stat.copy(totalUsageDuration = totalTimeInForeground)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., permission issues)
        }
    }

    override suspend fun getAppUsageStats(): List<AppUsageStats> {
        // Update usage durations before returning stats
        updateAppUsageDurations()
        return appUsageStatsDao.getAllAppUsageStats()
    }

    override suspend fun resetCooldownTimers() {
        appUsageStatsDao.resetAllCooldownTimers()
    }
}