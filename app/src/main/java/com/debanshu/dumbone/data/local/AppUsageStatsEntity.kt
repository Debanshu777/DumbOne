package com.debanshu.dumbone.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.debanshu.dumbone.data.model.AppUsageStats

@Entity(tableName = "app_usage_stats")
data class AppUsageStatsEntity(
    @PrimaryKey val packageName: String,
    val lastUsedTimestamp: Long,
    val usageCount: Int,
    val totalUsageDuration: Long,
    val currentCooldownExpiry: Long?
)

// Extension functions to convert between domain and entity models
fun AppUsageStatsEntity.toDomainModel() = AppUsageStats(
    packageName = packageName,
    lastUsedTimestamp = lastUsedTimestamp,
    usageCount = usageCount,
    totalUsageDuration = totalUsageDuration,
    currentCooldownExpiry = currentCooldownExpiry
)

fun AppUsageStats.toEntity() = AppUsageStatsEntity(
    packageName = packageName,
    lastUsedTimestamp = lastUsedTimestamp,
    usageCount = usageCount,
    totalUsageDuration = totalUsageDuration,
    currentCooldownExpiry = currentCooldownExpiry
)