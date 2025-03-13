package com.debanshu.dumbone.ui.screen.appListScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.TimerCalculator
import com.debanshu.dumbone.data.repository.AppRepository
import com.debanshu.dumbone.ui.screen.appListScreen.model.AppCategory
import com.debanshu.dumbone.ui.screen.appListScreen.model.AppListUiState
import com.debanshu.dumbone.ui.screen.appListScreen.model.AppWithCooldown
import com.debanshu.dumbone.ui.screen.appListScreen.model.CooldownInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the app list screen, handling both essential and limited access apps.
 */
@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository,
) : ViewModel() {


    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    private val _essentialApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _limitedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _cooldownRefreshTrigger = MutableStateFlow(System.currentTimeMillis())
    private val _searchQuery = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow(AppCategory.ALL)

    private val cooldownTicker = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    private val appUsageStats = _cooldownRefreshTrigger.map {
        appRepository.getAppUsageStats()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val cooldowns = combine(appUsageStats, cooldownTicker) { stats, currentTime ->
        stats.mapNotNull { stat ->
            stat.currentCooldownExpiry?.let { expiry ->
                if (expiry > currentTime) {
                    // Simple calculation for progress:
                    // 1. Get the remaining time
                    val remainingTime = expiry - currentTime

                    // 2. Determine approximately when the cooldown was set
                    // (using TimerCalculator's pattern)
                    val usageCount = stat.usageCount
                    val totalCooldownTime = TimerCalculator.calculateCooldownTime(usageCount)

                    // 3. Calculate elapsed time as a percentage of total time
                    val elapsedTime = totalCooldownTime - remainingTime
                    val progress = (elapsedTime.toFloat() / totalCooldownTime.toFloat()).coerceIn(0f, 1f)

                    Pair(stat.packageName, CooldownInfo(
                        isInCooldown = true,
                        timeRemaining = remainingTime,
                        progress = progress
                    ))
                } else {
                    Pair(stat.packageName, CooldownInfo(isInCooldown = false, timeRemaining = 0L, progress = 0f))
                }
            }
        }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val limitedAppsWithCooldowns = combine(
        _limitedApps,
        cooldowns,
        _searchQuery,
        _activeCategory
    ) { apps, cooldownMap, query, category ->
        apps.filter {
            it.appName.contains(query, ignoreCase = true) &&
                    (category == AppCategory.ALL || category == AppCategory.LIMITED)
        }.map { app ->
            val cooldownInfo = cooldownMap[app.packageName] ?: CooldownInfo()
            AppWithCooldown(
                appInfo = app,
                isInCooldown = cooldownInfo.isInCooldown,
                cooldownTimeRemaining = cooldownInfo.timeRemaining,
                cooldownProgress = cooldownInfo.progress
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredEssentialApps = combine(
        _essentialApps,
        _searchQuery,
        _activeCategory
    ) { apps, query, category ->
        apps.filter {
            it.appName.contains(query, ignoreCase = true) &&
                    (category == AppCategory.ALL || category == AppCategory.ESSENTIAL)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadApps()
        observeState()
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                filteredEssentialApps,
                limitedAppsWithCooldowns,
                _searchQuery,
                _activeCategory
            ) { essentialApps, limitedApps, query, category ->
                AppListUiState(
                    isLoading = false,
                    searchQuery = query,
                    activeCategory = category,
                    essentialApps = essentialApps,
                    limitedApps = limitedApps,
                    hasResults = essentialApps.isNotEmpty() || limitedApps.isNotEmpty()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val essential = appRepository.getEssentialApps()
                val limited = appRepository.getLimitedApps()

                _essentialApps.value = essential
                _limitedApps.value = limited

                refreshUsageStats()
            } catch (e: Exception) {
                // Handle error state
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun refreshUsageStats() {
        _cooldownRefreshTrigger.value = System.currentTimeMillis()
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            val cooldownInfo = cooldowns.value[packageName]
            if (cooldownInfo?.isInCooldown == true) {
                return@launch
            }
            appRepository.recordAppUsage(packageName)
            refreshUsageStats()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: AppCategory) {
        _activeCategory.value = category
    }
}
