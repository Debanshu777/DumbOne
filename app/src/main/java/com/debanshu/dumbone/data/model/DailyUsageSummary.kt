package com.debanshu.dumbone.data.model

data class DailyUsageSummary(
    val date: Long, // Timestamp for the day
    val totalScreenTime: Long, // Total screen time in milliseconds
    val appOpenCount: Int, // Number of app opens
    val notificationCount: Int, // Number of notifications received
    val unlockCount: Int, // Number of screen unlocks
    val productivityScore: Float // 0.0 to 1.0, calculated based on app usage patterns
)