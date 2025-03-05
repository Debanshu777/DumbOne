package com.debanshu.dumbone

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class DumbOneApp: Application() {
    override fun onCreate() {
        super.onCreate()

        val resetWorkRequest = PeriodicWorkRequestBuilder<DailyCooldownResetWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_cooldown_reset",
            ExistingPeriodicWorkPolicy.KEEP,
            resetWorkRequest
        )
    }
}