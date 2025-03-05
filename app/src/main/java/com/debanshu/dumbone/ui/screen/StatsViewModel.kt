package com.debanshu.dumbone.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.AppUsageStats
import com.debanshu.dumbone.data.repository.AppRepository
import com.debanshu.dumbone.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _usageStats = MutableStateFlow<List<AppUsageStats>>(emptyList())
    val usageStats: StateFlow<List<AppUsageStats>> = _usageStats.asStateFlow()

    private val _allApps = MutableStateFlow<Map<String, AppInfo>>(emptyMap())
    val allApps: StateFlow<Map<String, AppInfo>> = _allApps.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val apps = appRepository.getInstalledApps().associateBy { it.packageName }
            _allApps.value = apps

            _usageStats.value = appRepository.getAppUsageStats()
        }
    }

    fun refreshData() {
        loadData()
    }
}