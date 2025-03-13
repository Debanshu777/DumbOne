package com.debanshu.dumbone.ui.screen.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.repository.AppRepository
import com.debanshu.dumbone.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        loadData()
        viewModelScope.launch {
            preferencesRepository.userPreferences.collectLatest {
                _isOnboardingCompleted.tryEmit(it.onboardingCompleted)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Check if onboarding is completed
            val userPrefs = preferencesRepository.userPreferences.first()
            _isOnboardingCompleted.tryEmit(userPrefs.onboardingCompleted)

            // Load essential apps
            _essentialApps.tryEmit(appRepository.getEssentialApps())
        }
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            appRepository.recordAppUsage(packageName)
        }
    }
}