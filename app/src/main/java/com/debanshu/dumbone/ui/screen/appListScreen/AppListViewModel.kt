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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    // --- State flows ---
    private val _essentialApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _limitedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _cooldowns = MutableStateFlow<Map<String, CooldownInfo>>(emptyMap())
    private val _totalCooldownTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val _searchQuery = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow(AppCategory.ALL)
    private val _isLoading = MutableStateFlow(true)

    // --- UI State ---
    private val _uiState = MutableStateFlow(AppListUiState())
    internal val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    // --- Cooldown ticker job ---
    private var cooldownTickerJob: Job? = null

    // --- Derived states ---
    private val filteredEssentialApps = combine(
        _essentialApps,
        _searchQuery,
        _activeCategory
    ) { apps, query, category ->
        apps.filter {
            it.appName.contains(query, ignoreCase = true) &&
                    (category == AppCategory.ALL || category == AppCategory.ESSENTIAL)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val limitedAppsWithCooldowns = combine(
        _limitedApps,
        _cooldowns,
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

    init {
        loadApps()
        observeState()
    }

    /**
     * Observe changes to the state and update the UI state
     */
    private fun observeState() {
        viewModelScope.launch {
            combine(
                filteredEssentialApps,
                limitedAppsWithCooldowns,
                _searchQuery,
                _activeCategory,
                _isLoading
            ) { essential, limited, query, category, isLoading ->
                AppListUiState(
                    isLoading = isLoading,
                    searchQuery = query,
                    activeCategory = category,
                    essentialApps = essential,
                    limitedApps = limited,
                    hasResults = essential.isNotEmpty() || limited.isNotEmpty()
                )
            }.collect { state ->
                _uiState.value = state

                // Start or stop ticker based on active cooldowns
                val hasActiveCooldowns = _cooldowns.value.any { it.value.isInCooldown }
                if (hasActiveCooldowns) {
                    startCooldownTicker()
                } else {
                    stopCooldownTicker()
                }
            }
        }
    }

    /**
     * Load apps and cooldown information
     */
    private fun loadApps() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Load apps
                val essential = appRepository.getEssentialApps()
                val limited = appRepository.getLimitedApps()

                _essentialApps.value = essential
                _limitedApps.value = limited

                // Load cooldowns
                refreshCooldowns()

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh cooldown information from repository
     */
    private fun refreshCooldowns() {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val stats = appRepository.getAppUsageStats()

                val cooldowns = mutableMapOf<String, CooldownInfo>()
                val totalTimes = mutableMapOf<String, Long>()

                for (stat in stats) {
                    val expiry = stat.currentCooldownExpiry
                    if (expiry != null && expiry > currentTime) {
                        // App is in cooldown
                        val remainingTime = expiry - currentTime
                        val totalTime = TimerCalculator.calculateCooldownTime(stat.usageCount)

                        totalTimes[stat.packageName] = totalTime

                        val elapsedTime = totalTime - remainingTime
                        val progress =
                            (elapsedTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)

                        cooldowns[stat.packageName] = CooldownInfo(
                            isInCooldown = true,
                            timeRemaining = remainingTime,
                            progress = progress
                        )
                    } else {
                        // App is not in cooldown
                        cooldowns[stat.packageName] = CooldownInfo()
                        totalTimes[stat.packageName] = 0L
                    }
                }

                _cooldowns.value = cooldowns
                _totalCooldownTimes.value = totalTimes
            } catch (e: Exception) {
                // Handle errors if needed
            }
        }
    }

    /**
     * Start the cooldown ticker to update timers every second
     */
    private fun startCooldownTicker() {
        if (cooldownTickerJob != null) return

        cooldownTickerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // 1 second delay
                updateCooldownsLocally()

                // Stop if no more active cooldowns
                if (_cooldowns.value.none { it.value.isInCooldown }) {
                    break
                }
            }
        }
    }

    /**
     * Stop the cooldown ticker
     */
    private fun stopCooldownTicker() {
        cooldownTickerJob?.cancel()
        cooldownTickerJob = null
    }

    /**
     * Update cooldowns locally without fetching from repository
     */
    private fun updateCooldownsLocally() {
        val currentCooldowns = _cooldowns.value
        val totalTimes = _totalCooldownTimes.value
        val updatedCooldowns = mutableMapOf<String, CooldownInfo>()

        for ((packageName, cooldownInfo) in currentCooldowns) {
            if (!cooldownInfo.isInCooldown) {
                updatedCooldowns[packageName] = cooldownInfo
                continue
            }

            // Reduce remaining time by 1 second
            val newRemainingTime = (cooldownInfo.timeRemaining - 1000).coerceAtLeast(0L)
            val totalTime = totalTimes[packageName] ?: 0L

            if (newRemainingTime <= 0) {
                // Cooldown expired
                updatedCooldowns[packageName] = CooldownInfo()
            } else {
                // Update progress
                val elapsedTime = totalTime - newRemainingTime
                val progress = if (totalTime > 0) {
                    (elapsedTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }

                updatedCooldowns[packageName] = CooldownInfo(
                    isInCooldown = true,
                    timeRemaining = newRemainingTime,
                    progress = progress
                )
            }
        }

        _cooldowns.value = updatedCooldowns
    }

    /**
     * Launch an app and record usage
     */
    fun launchApp(packageName: String) {
        viewModelScope.launch {
            try {
                // Check if app is in cooldown
                val cooldownInfo = _cooldowns.value[packageName]
                if (cooldownInfo?.isInCooldown == true) {
                    return@launch
                }

                // Record app usage
                appRepository.recordAppUsage(packageName)

                // Refresh cooldowns to get updated state
                refreshCooldowns()
            } catch (e: Exception) {
                // Handle errors if needed
            }
        }
    }

    /**
     * Handle search query changes
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Handle category selection changes
     */
    internal fun onCategorySelected(category: AppCategory) {
        _activeCategory.value = category
    }

    override fun onCleared() {
        super.onCleared()
        stopCooldownTicker()
    }
}