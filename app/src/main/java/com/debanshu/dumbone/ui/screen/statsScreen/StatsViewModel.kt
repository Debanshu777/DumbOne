package com.debanshu.dumbone.ui.screen.statsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppCategory
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.AppUsageStats
import com.debanshu.dumbone.data.model.DailyUsageSummary
import com.debanshu.dumbone.data.model.HourlyUsage
import com.debanshu.dumbone.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    // Basic stats data
    private val _usageStats = MutableStateFlow<List<AppUsageStats>>(emptyList())
    val usageStats: StateFlow<List<AppUsageStats>> = _usageStats.asStateFlow()

    private val _allApps = MutableStateFlow<Map<String, AppInfo>>(emptyMap())
    val allApps: StateFlow<Map<String, AppInfo>> = _allApps.asStateFlow()

    private val _needsPermission = MutableStateFlow(false)
    val needsPermission: StateFlow<Boolean> = _needsPermission.asStateFlow()

    // Enhanced stats data
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _contributionData = MutableStateFlow<Map<Long, DailyUsageSummary>>(emptyMap())
    val contributionData: StateFlow<Map<Long, DailyUsageSummary>> = _contributionData.asStateFlow()

    private val _hourlyData = MutableStateFlow<List<HourlyUsage>>(emptyList())
    val hourlyData: StateFlow<List<HourlyUsage>> = _hourlyData.asStateFlow()

    private val _dailyScreenTime = MutableStateFlow(0L)
    val dailyScreenTime: StateFlow<Long> = _dailyScreenTime.asStateFlow()

    private val _appOpenCount = MutableStateFlow(0)
    val appOpenCount: StateFlow<Int> = _appOpenCount.asStateFlow()

    private val _notificationCount = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = _notificationCount.asStateFlow()

    private val _unlockCount = MutableStateFlow(0)
    val unlockCount: StateFlow<Int> = _unlockCount.asStateFlow()

    private val _productivityScore = MutableStateFlow(0f)
    val productivityScore: StateFlow<Float> = _productivityScore.asStateFlow()

    private val _appUsageSummary = MutableStateFlow<List<AppRepository.AppUsageSummary>>(emptyList())
    val appUsageSummary: StateFlow<List<AppRepository.AppUsageSummary>> = _appUsageSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Usage by category stats
    private val _categoryStats = MutableStateFlow<Map<AppCategory, Long>>(emptyMap())
    val categoryStats: StateFlow<Map<AppCategory, Long>> = _categoryStats.asStateFlow()

    // Currently selected view mode (0: Contribution, 1: Circular, 2: Bar)
    private val _selectedViewMode = MutableStateFlow(0)
    val selectedViewMode: StateFlow<Int> = _selectedViewMode.asStateFlow()

    // Developer toggle for real/simulated data
    private val _useRealData = MutableStateFlow(false)
    val useRealData: StateFlow<Boolean> = _useRealData.asStateFlow()

    init {
        checkPermission()
        loadData()
        loadContributionData()
    }

    private fun checkPermission() {
        _needsPermission.value = !appRepository.hasUsageStatsPermission()
    }

    fun toggleDataMode() {
        _useRealData.value = !_useRealData.value
        loadData()
        loadContributionData()
    }

    fun setSelectedDate(timestamp: Long) {
        _selectedDate.value = timestamp
        loadDailyData(timestamp)
    }

    fun setViewMode(mode: Int) {
        _selectedViewMode.value = mode
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            val apps = appRepository.getInstalledApps().associateBy { it.packageName }
            _allApps.value = apps

            _usageStats.value = appRepository.getAppUsageStats()

            // Load data for current day by default
            loadDailyData(System.currentTimeMillis())

            _isLoading.value = false
        }
    }

    private fun loadContributionData() {
        viewModelScope.launch {
            // Fetch 90 days of data (approximately 3 months)
            _contributionData.value = appRepository.getDailyUsageSummaries(90, _useRealData.value)
        }
    }

    private fun loadDailyData(timestamp: Long) {
        viewModelScope.launch {
            // Get detailed stats for the selected day
            val forceRealData = _useRealData.value
            _hourlyData.value = appRepository.getHourlyUsageForDay(timestamp, forceRealData)
            _dailyScreenTime.value = appRepository.getTotalScreenTime(timestamp, forceRealData)
            _appOpenCount.value = appRepository.getAppOpenCount(timestamp, forceRealData)
            _notificationCount.value = appRepository.getNotificationCount(timestamp, forceRealData)
            _unlockCount.value = appRepository.getScreenUnlockCount(timestamp, forceRealData)
            _productivityScore.value = appRepository.calculateProductivityScore(timestamp, forceRealData)

            // App usage summaries
            val appSummaries = appRepository.getAppUsageSummary(timestamp, forceRealData)
            _appUsageSummary.value = appSummaries

            // Calculate category stats
            _categoryStats.value = calculateCategoryStats(appSummaries)
        }
    }

    private fun calculateCategoryStats(appSummaries: List<AppRepository.AppUsageSummary>): Map<AppCategory, Long> {
        return appSummaries.groupBy { it.category }
            .mapValues { (_, apps) -> apps.sumOf { it.usageTime } }
            .filter { it.value > 0 }
    }

    fun refreshData() {
        checkPermission()
        loadData()
        loadContributionData()
    }

    fun requestUsageStatsPermission() {
        appRepository.requestUsageStatsPermission()
    }
}