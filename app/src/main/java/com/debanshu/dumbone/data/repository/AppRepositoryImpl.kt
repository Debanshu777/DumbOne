package com.debanshu.dumbone.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.palette.graphics.Palette
import com.debanshu.dumbone.data.local.AppUsageStatsDao
import com.debanshu.dumbone.data.local.getAllAppUsageStats
import com.debanshu.dumbone.data.local.getAppUsageStats
import com.debanshu.dumbone.data.local.insertAppUsageStats
import com.debanshu.dumbone.data.local.resetAllCooldownTimers
import com.debanshu.dumbone.data.local.updateAppUsageStats
import com.debanshu.dumbone.data.model.AppCategory
import com.debanshu.dumbone.data.model.AppCategoryInfo
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.AppUsageStats
import com.debanshu.dumbone.data.model.DailyUsageSummary
import com.debanshu.dumbone.data.model.HourlyUsage
import com.debanshu.dumbone.data.model.TimerCalculator
import com.debanshu.dumbone.data.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val appUsageStatsDao: AppUsageStatsDao
) : AppRepository {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val random = Random(System.currentTimeMillis())


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

    override suspend fun getDailyUsageSummaries(daysToFetch: Int, forceRealData: Boolean): Map<Long, DailyUsageSummary> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission() || usageStatsManager == null || !forceRealData) {
            return@withContext generateSimulatedDailySummaries(daysToFetch)
        }

        try {
            val summaries = mutableMapOf<Long, DailyUsageSummary>()
            val currentTimeMillis = System.currentTimeMillis()

            for (i in 0 until daysToFetch) {
                val dayStart = getDayStartMillis(currentTimeMillis - (i * 24 * 60 * 60 * 1000))

                val screenTime = getTotalScreenTime(dayStart, true)
                val appOpenCount = getAppOpenCount(dayStart, true)
                val notificationCount = getNotificationCount(dayStart, true)
                val unlockCount = getScreenUnlockCount(dayStart, true)
                val productivityScore = calculateProductivityScore(dayStart, true)

                summaries[dayStart] = DailyUsageSummary(
                    date = dayStart,
                    totalScreenTime = screenTime,
                    appOpenCount = appOpenCount,
                    notificationCount = notificationCount,
                    unlockCount = unlockCount,
                    productivityScore = productivityScore
                )
            }

            if (summaries.isNotEmpty()) {
                return@withContext summaries
            }
        } catch (e: Exception) {
            // Fall back to simulated data on error
        }

        return@withContext generateSimulatedDailySummaries(daysToFetch)
    }

    private fun generateSimulatedDailySummaries(daysToFetch: Int): Map<Long, DailyUsageSummary> {
        val summaries = mutableMapOf<Long, DailyUsageSummary>()
        val currentTimeMillis = System.currentTimeMillis()

        for (i in 0 until daysToFetch) {
            val dayStart = getDayStartMillis(currentTimeMillis - (i * 24 * 60 * 60 * 1000))

            // Skip some days to create a more realistic pattern
            if (random.nextFloat() < 0.1f) {
                continue // 10% chance to skip a day
            }

            val screenTime = ((30 + random.nextInt(330)) * 60 * 1000).toLong()
            val appOpenCount = 15 + random.nextInt(85)
            val notificationCount = 20 + random.nextInt(130)
            val unlockCount = 10 + random.nextInt(40)
            val productivityScore = (0.3f + random.nextFloat() * 0.6f)

            summaries[dayStart] = DailyUsageSummary(
                date = dayStart,
                totalScreenTime = screenTime,
                appOpenCount = appOpenCount,
                notificationCount = notificationCount,
                unlockCount = unlockCount,
                productivityScore = productivityScore
            )
        }

        return summaries
    }

    override suspend fun getHourlyUsageForDay(timestamp: Long, forceRealData: Boolean): List<HourlyUsage> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission() || usageStatsManager == null || !forceRealData) {
            return@withContext generateSimulatedHourlyData()
        }

        val dayStart = getDayStartMillis(timestamp)
        val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1

        try {
            val hourlyUsages = MutableList(24) { hour ->
                HourlyUsage(
                    hour = hour,
                    screenTime = 0L,
                    appOpens = 0,
                    productiveTime = 0L,
                    distractingTime = 0L
                )
            }

            // Get app productivity categories
            val productivityMap = getAppProductivityCategories().associateBy { it.packageName }

            // Query events for the day
            val events = usageStatsManager.queryEvents(dayStart, dayEnd)
            val event = UsageEvents.Event()

            // Track foreground sessions
            val foregroundSessions = mutableMapOf<String, Long>() // packageName to start time

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val packageName = event.packageName
                val eventTimestamp = event.timeStamp
                val hour = getHourFromTimestamp(eventTimestamp)

                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        foregroundSessions[packageName] = eventTimestamp
                        hourlyUsages[hour] = hourlyUsages[hour].copy(
                            appOpens = hourlyUsages[hour].appOpens + 1
                        )
                    }
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val startTime = foregroundSessions[packageName] ?: continue
                        val duration = eventTimestamp - startTime

                        if (duration > 0) {
                            val startHour = getHourFromTimestamp(startTime)
                            val endHour = getHourFromTimestamp(eventTimestamp)

                            // Simple case: same hour
                            if (startHour == endHour) {
                                updateHourlyData(hourlyUsages, startHour, duration, packageName, productivityMap)
                            }
                            // Complex case: spans multiple hours
                            else {
                                // Proportionally distribute time across hours
                                var remainingTime = duration
                                var currentHour = startHour

                                while (currentHour != endHour && remainingTime > 0) {
                                    val hourEndTime = getHourEndMillis(startTime, currentHour)
                                    val timeInHour = minOf(hourEndTime - startTime, remainingTime)

                                    updateHourlyData(hourlyUsages, currentHour, timeInHour, packageName, productivityMap)

                                    remainingTime -= timeInHour
                                    currentHour = (currentHour + 1) % 24
                                }

                                // Last hour
                                if (remainingTime > 0) {
                                    updateHourlyData(hourlyUsages, endHour, remainingTime, packageName, productivityMap)
                                }
                            }
                        }

                        // Clear the session
                        foregroundSessions.remove(packageName)
                    }
                }
            }

            val result = hourlyUsages.filterNot { it.screenTime <= 0 && it.appOpens <= 0 }
            if (result.isNotEmpty()) {
                return@withContext result
            }
        } catch (e: Exception) {
            // Fall back to simulated data on error
        }

        return@withContext generateSimulatedHourlyData()
    }

    private fun updateHourlyData(
        hourlyUsages: MutableList<HourlyUsage>,
        hour: Int,
        duration: Long,
        packageName: String,
        productivityMap: Map<String, AppCategoryInfo>
    ) {
        val isProductiveApp = productivityMap[packageName]?.isProductiveApp ?: false
        val currentHourData = hourlyUsages[hour]

        hourlyUsages[hour] = currentHourData.copy(
            screenTime = currentHourData.screenTime + duration,
            productiveTime = currentHourData.productiveTime + if (isProductiveApp) duration else 0,
            distractingTime = currentHourData.distractingTime + if (!isProductiveApp) duration else 0
        )
    }

    override suspend fun getAppProductivityCategories(): List<AppCategoryInfo> = withContext(Dispatchers.IO) {
        val allApps = getInstalledApps()
        return@withContext allApps.map { app ->
            val category = AppCategory.fromPackageName(app.packageName)
            val isProductiveApp = category == AppCategory.PRODUCTIVITY ||
                    category == AppCategory.UTILITY ||
                    category == AppCategory.COMMUNICATION

            AppCategoryInfo(
                packageName = app.packageName,
                isProductiveApp = isProductiveApp,
                category = category
            )
        }
    }

    override suspend fun calculateProductivityScore(timestamp: Long, forceRealData: Boolean): Float = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission() || usageStatsManager == null || !forceRealData) {
            return@withContext generateSimulatedProductivityScore()
        }

        try {
            val dayStart = getDayStartMillis(timestamp)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                dayStart,
                dayEnd
            )

            var productiveTime = 0L
            var distractingTime = 0L

            // Get productivity categories
            val productivityMap = getAppProductivityCategories().associateBy { it.packageName }

            // Calculate time spent in each category
            for (stat in usageStats) {
                val packageName = stat.packageName
                val appInfo = productivityMap[packageName]

                if (appInfo?.isProductiveApp == true) {
                    productiveTime += stat.totalTimeInForeground
                } else {
                    distractingTime += stat.totalTimeInForeground
                }
            }

            // Calculate productivity score (0.0 to 1.0)
            val totalTime = productiveTime + distractingTime
            if (totalTime > 0) {
                return@withContext (productiveTime.toFloat() / totalTime).coerceIn(0f, 1f)
            }
        } catch (e: Exception) {
            // Fall back to simulated data on error
        }

        return@withContext generateSimulatedProductivityScore()
    }

    override suspend fun getTotalScreenTime(timestamp: Long, forceRealData: Boolean): Long = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission() || usageStatsManager == null || !forceRealData) {
            return@withContext generateSimulatedScreenTime()
        }

        try {
            val dayStart = getDayStartMillis(timestamp)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                dayStart,
                dayEnd
            )

            val totalTime = usageStats.sumOf { it.totalTimeInForeground }
            if (totalTime > 0) {
                return@withContext totalTime
            }
        } catch (e: Exception) {
            // Fall back to simulated data on error
        }

        return@withContext generateSimulatedScreenTime()
    }

    override suspend fun getAppOpenCount(timestamp: Long, forceRealData: Boolean): Int = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission() || usageStatsManager == null || !forceRealData) {
            return@withContext generateSimulatedAppOpenCount()
        }

        try {
            val dayStart = getDayStartMillis(timestamp)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1

            val events = usageStatsManager.queryEvents(dayStart, dayEnd)
            val event = UsageEvents.Event()
            var count = 0

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    count++
                }
            }

            if (count > 0) {
                return@withContext count
            }
        } catch (e: Exception) {
            // Fall back to simulated data on error
        }

        return@withContext generateSimulatedAppOpenCount()
    }

    override suspend fun getNotificationCount(timestamp: Long, forceRealData: Boolean): Int = withContext(Dispatchers.IO) {
        // Currently returns simulated data - would require NotificationListenerService in a real app
        return@withContext generateSimulatedNotificationCount()
    }

    override suspend fun getScreenUnlockCount(timestamp: Long, forceRealData: Boolean): Int = withContext(Dispatchers.IO) {
        // Currently returns simulated data - would require device admin permissions
        return@withContext generateSimulatedUnlockCount()
    }

    override suspend fun getAppUsageSummary(timestamp: Long, forceRealData: Boolean): List<AppRepository.AppUsageSummary> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission() || usageStatsManager == null || !forceRealData) {
            return@withContext generateSimulatedAppUsageSummary()
        }

        try {
            val dayStart = getDayStartMillis(timestamp)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                dayStart,
                dayEnd
            )

            val productivityInfoMap = getAppProductivityCategories().associateBy { it.packageName }
            val packageManager = context.packageManager
            val appOpenCounts = mutableMapOf<String, Int>()

            // Get open counts
            val events = usageStatsManager.queryEvents(dayStart, dayEnd)
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    val packageName = event.packageName
                    appOpenCounts[packageName] = (appOpenCounts[packageName] ?: 0) + 1
                }
            }

            val result = usageStats
                .filter { it.totalTimeInForeground > 0 }
                .map { stat ->
                    val packageName = stat.packageName
                    val productivityInfo = productivityInfoMap[packageName]

                    // Try to get app name from package manager
                    val appName = try {
                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        packageName.substringAfterLast('.')
                    }

                    AppRepository.AppUsageSummary(
                        packageName = packageName,
                        appName = appName,
                        usageTime = stat.totalTimeInForeground,
                        openCount = appOpenCounts[packageName] ?: 0,
                        isProductiveApp = productivityInfo?.isProductiveApp ?: false,
                        category = productivityInfo?.category ?: AppCategory.OTHER
                    )
                }
                .sortedByDescending { it.usageTime }

            if (result.isNotEmpty()) {
                return@withContext result
            }
        } catch (e: Exception) {
            // Fall back to simulated data on error
        }

        return@withContext generateSimulatedAppUsageSummary()
    }

    // Helper methods
    private fun getDayStartMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getHourFromTimestamp(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    private fun getHourEndMillis(timestamp: Long, hour: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // Data simulation methods
    private fun generateSimulatedScreenTime(): Long {
        return ((30 + random.nextInt(330)) * 60 * 1000).toLong()
    }

    private fun generateSimulatedProductivityScore(): Float {
        return (0.3f + random.nextFloat() * 0.6f)
    }

    private fun generateSimulatedAppOpenCount(): Int {
        return 15 + random.nextInt(85)
    }

    private fun generateSimulatedNotificationCount(): Int {
        return 20 + random.nextInt(130)
    }

    private fun generateSimulatedUnlockCount(): Int {
        return 10 + random.nextInt(40)
    }

    private fun generateSimulatedHourlyData(): List<HourlyUsage> {
        val result = mutableListOf<HourlyUsage>()

        // Generate active hours (9am-11pm)
        for (hour in 9..23) {
            if (random.nextFloat() < 0.8f) { // 80% chance of having data for this hour
                val screenTime = (5 + random.nextInt(56)) * 60 * 1000L // 5-60 minutes
                val productiveRatio = random.nextFloat()

                result.add(
                    HourlyUsage(
                        hour = hour,
                        screenTime = screenTime,
                        appOpens = 1 + random.nextInt(10),
                        productiveTime = (screenTime * productiveRatio).toLong(),
                        distractingTime = (screenTime * (1 - productiveRatio)).toLong()
                    )
                )
            }
        }

        return result
    }

    private fun generateSimulatedAppUsageSummary(): List<AppRepository.AppUsageSummary> {
        val result = mutableListOf<AppRepository.AppUsageSummary>()

        // Common apps
        val commonApps = listOf(
            Triple("com.whatsapp", "WhatsApp", AppCategory.COMMUNICATION),
            Triple("com.instagram.android", "Instagram", AppCategory.SOCIAL),
            Triple("com.google.android.youtube", "YouTube", AppCategory.ENTERTAINMENT),
            Triple("com.google.android.gm", "Gmail", AppCategory.PRODUCTIVITY),
            Triple("com.android.chrome", "Chrome", AppCategory.PRODUCTIVITY),
            Triple("com.facebook.katana", "Facebook", AppCategory.SOCIAL),
            Triple("com.twitter.android", "Twitter", AppCategory.SOCIAL),
            Triple("com.spotify.music", "Spotify", AppCategory.ENTERTAINMENT),
            Triple("com.slack", "Slack", AppCategory.PRODUCTIVITY),
            Triple("com.google.android.apps.meetings", "Google Meet", AppCategory.PRODUCTIVITY)
        )

        // Add 6-10 apps
        val numApps = 6 + random.nextInt(5)
        for (i in 0 until numApps) {
            val appInfo = if (i < commonApps.size) {
                commonApps[i]
            } else {
                Triple(
                    "com.example.app$i",
                    "App ${i + 1}",
                    AppCategory.values()[random.nextInt(AppCategory.values().size)]
                )
            }

            val (packageName, appName, category) = appInfo
            val isProductiveApp = category == AppCategory.PRODUCTIVITY ||
                    category == AppCategory.UTILITY ||
                    category == AppCategory.COMMUNICATION

            // Generate usage time between 5 minutes and 2 hours
            val usageTime = ((5 + random.nextInt(115)) * 60 * 1000).toLong()

            result.add(
                AppRepository.AppUsageSummary(
                    packageName = packageName,
                    appName = appName,
                    usageTime = usageTime,
                    openCount = 1 + random.nextInt(20),
                    isProductiveApp = isProductiveApp,
                    category = category
                )
            )
        }

        return result.sortedByDescending { it.usageTime }
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
            (drawable as? BitmapDrawable)?.bitmap ?: createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(
                    1
                ), drawable.intrinsicHeight.coerceAtLeast(1)
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