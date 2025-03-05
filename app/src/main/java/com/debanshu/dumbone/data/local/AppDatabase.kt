package com.debanshu.dumbone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppUsageStatsEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageStatsDao(): AppUsageStatsDao
}