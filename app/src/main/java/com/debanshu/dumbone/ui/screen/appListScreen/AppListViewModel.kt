package com.debanshu.dumbone.ui.screen.appListScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository,
) : ViewModel() {

    private val _essentialApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val essentialApps: StateFlow<List<AppInfo>> = _essentialApps.asStateFlow()

    private val _limitedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    // Ticker that emits every second to update cooldowns
    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // Track when app usage stats should be refreshed
    private val _refreshTrigger = MutableStateFlow(0L)

    // Fetch app usage stats when refresh is triggered
    private val appUsageStats = _refreshTrigger.flatMapLatest {
        flow {
            emit(appRepository.getAppUsageStats())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculate cooldowns based on app usage stats and current time
    private val cooldowns = combine(appUsageStats, ticker) { stats, currentTime ->
        stats.mapNotNull { stat ->
            stat.currentCooldownExpiry?.let { expiry ->
                if (expiry > currentTime) {
                    stat.packageName to (expiry - currentTime)
                } else null
            }
        }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Combine limited apps with cooldowns for UI consumption
    val limitedAppsWithCooldowns = combine(_limitedApps, cooldowns) { apps, cooldownMap ->
        apps.map { app ->
            val cooldownTime = cooldownMap[app.packageName] ?: 0L
            AppWithCooldown(
                appInfo = app,
                isInCooldown = cooldownTime > 0,
                cooldownTimeRemaining = cooldownTime
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _essentialApps.value = appRepository.getEssentialApps()
            _limitedApps.value = appRepository.getLimitedApps()
            refreshUsageStats()
        }
    }

    private fun refreshUsageStats() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            appRepository.recordAppUsage(packageName)
            refreshUsageStats()
        }
    }
}

// Data class to combine app info with cooldown state
data class AppWithCooldown(
    val appInfo: AppInfo,
    val isInCooldown: Boolean,
    val cooldownTimeRemaining: Long
)