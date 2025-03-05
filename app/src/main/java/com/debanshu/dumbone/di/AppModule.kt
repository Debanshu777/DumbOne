package com.debanshu.dumbone.di

import android.content.Context
import androidx.room.Room
import com.debanshu.dumbone.data.local.AppDatabase
import com.debanshu.dumbone.data.local.AppUsageStatsDao
import com.debanshu.dumbone.data.repository.AppRepository
import com.debanshu.dumbone.data.repository.AppRepositoryImpl
import com.debanshu.dumbone.data.repository.PreferencesRepository
import com.debanshu.dumbone.data.repository.PreferencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dumb_one_launcher_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAppUsageStatsDao(database: AppDatabase): AppUsageStatsDao {
        return database.appUsageStatsDao()
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        preferencesRepository: PreferencesRepository,
        appUsageStatsDao: AppUsageStatsDao
    ): AppRepository {
        return AppRepositoryImpl(context, preferencesRepository, appUsageStatsDao)
    }
}