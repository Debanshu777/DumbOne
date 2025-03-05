package com.debanshu.dumbone.data.model

data class AppUsageStats(
    val packageName: String,
    val lastUsedTimestamp: Long,
    val usageCount: Int,
    val totalUsageDuration: Long,    // In milliseconds
    val currentCooldownExpiry: Long? // Timestamp when cooldown expires (null if not in cooldown)
)