package com.debanshu.dumbone.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.debanshu.dumbone.data.model.AppUsageStats

@Dao
interface AppUsageStatsDao {
    @Query("SELECT * FROM app_usage_stats WHERE packageName = :packageName")
    suspend fun getAppUsageStatsEntity(packageName: String): AppUsageStatsEntity?

    @Query("SELECT * FROM app_usage_stats")
    suspend fun getAllAppUsageStatsEntities(): List<AppUsageStatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsageStatsEntity(entity: AppUsageStatsEntity)

    @Update
    suspend fun updateAppUsageStatsEntity(entity: AppUsageStatsEntity)

    @Query("UPDATE app_usage_stats SET currentCooldownExpiry = NULL")
    suspend fun resetAllCooldownTimersEntity()
}

suspend fun AppUsageStatsDao.getAppUsageStats(packageName: String): AppUsageStats? {
    return getAppUsageStatsEntity(packageName)?.toDomainModel()
}

suspend fun AppUsageStatsDao.getAllAppUsageStats(): List<AppUsageStats> {
    return getAllAppUsageStatsEntities().map { it.toDomainModel() }
}

suspend fun AppUsageStatsDao.insertAppUsageStats(stats: AppUsageStats) {
    insertAppUsageStatsEntity(stats.toEntity())
}

suspend fun AppUsageStatsDao.updateAppUsageStats(stats: AppUsageStats) {
    updateAppUsageStatsEntity(stats.toEntity())
}

suspend fun AppUsageStatsDao.resetAllCooldownTimers() {
    resetAllCooldownTimersEntity()
}