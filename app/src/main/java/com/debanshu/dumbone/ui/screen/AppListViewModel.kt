package com.debanshu.dumbone.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.repository.AppRepository
import com.debanshu.dumbone.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _essentialApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val essentialApps: StateFlow<List<AppInfo>> = _essentialApps.asStateFlow()

    private val _limitedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val limitedApps: StateFlow<List<AppInfo>> = _limitedApps.asStateFlow()

    private val _appCooldowns = MutableStateFlow<Map<String, Long>>(emptyMap())
    val appCooldowns: StateFlow<Map<String, Long>> = _appCooldowns.asStateFlow()

    init {
        loadApps()
        // Periodically check app cooldowns
        viewModelScope.launch {
            while (true) {
                updateCooldowns()
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _essentialApps.value = appRepository.getEssentialApps()
            _limitedApps.value = appRepository.getLimitedApps()
            updateCooldowns()
        }
    }

    private suspend fun updateCooldowns() {
        val usageStats = appRepository.getAppUsageStats()
        val currentTime = System.currentTimeMillis()

        val cooldowns = usageStats.mapNotNull { stats ->
            stats.currentCooldownExpiry?.let { expiry ->
                if (expiry > currentTime) {
                    stats.packageName to (expiry - currentTime)
                } else null
            }
        }.toMap()

        _appCooldowns.value = cooldowns
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            appRepository.recordAppUsage(packageName)
            updateCooldowns()
        }
    }

    fun isAppInCooldown(packageName: String): Boolean {
        return _appCooldowns.value.containsKey(packageName)
    }

    fun getCooldownTimeRemaining(packageName: String): Long {
        return _appCooldowns.value[packageName] ?: 0L
    }
}