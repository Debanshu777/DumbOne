package com.debanshu.dumbone.ui.screen.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.repository.AppRepository
import com.debanshu.dumbone.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _essentialApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val essentialApps: StateFlow<List<AppInfo>> = _essentialApps.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        loadData()
        // Update the time every minute
        viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                delay(60000) // 1 minute
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Check if onboarding is completed
            val userPrefs = preferencesRepository.userPreferences.first()
            _isOnboardingCompleted.value = userPrefs.onboardingCompleted

            // Load essential apps
            _essentialApps.value = appRepository.getEssentialApps()
        }
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            appRepository.recordAppUsage(packageName)
        }
    }
}