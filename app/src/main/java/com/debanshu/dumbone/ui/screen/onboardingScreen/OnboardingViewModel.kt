package com.debanshu.dumbone.ui.screen.onboardingScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.dumbone.data.model.AppInfo
import com.debanshu.dumbone.data.model.UserPreferences
import com.debanshu.dumbone.data.repository.AppRepository
import com.debanshu.dumbone.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _selectedEssentialApps = MutableStateFlow<List<String>>(emptyList())
    val selectedEssentialApps: StateFlow<List<String>> = _selectedEssentialApps.asStateFlow()

    private val _selectedLimitedApps = MutableStateFlow<List<String>>(emptyList())
    val selectedLimitedApps: StateFlow<List<String>> = _selectedLimitedApps.asStateFlow()

    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    // Default apps that should be pre-selected
    private val defaultEssentialApps = listOf(
        "com.android.dialer", // Phone
        "com.android.messaging", // Messages
        "com.android.chrome" // Chrome
    )

    // Addictive apps that should be in limited access by default
    private val defaultLimitedApps = listOf(
        "com.instagram.android", // Instagram
        "com.facebook.katana", // Facebook
        "com.twitter.android", // Twitter/X
        "com.snapchat.android", // Snapchat
        "com.tiktok.tiktok", // TikTok
        "com.zhiliaoapp.musically" // TikTok alternative package
    )

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = appRepository.getInstalledApps()
            _allApps.value = apps

            // Pre-select default essential apps that are installed
            val installedEssentialApps = apps
                .filter { it.packageName in defaultEssentialApps }
                .map { it.packageName }
            _selectedEssentialApps.value = installedEssentialApps

            // Pre-select default limited apps that are installed
            val installedLimitedApps = apps
                .filter { it.packageName in defaultLimitedApps }
                .map { it.packageName }
            _selectedLimitedApps.value = installedLimitedApps
        }
    }

    fun toggleEssentialApp(packageName: String, selected: Boolean) {
        val currentList = _selectedEssentialApps.value.toMutableList()

        if (selected) {
            // Check if we already have 5 apps selected
            if (currentList.size >= 5 && !currentList.contains(packageName)) {
                return
            }

            // Add to essential, remove from limited if present
            if (!currentList.contains(packageName)) {
                currentList.add(packageName)
            }

            val limitedList = _selectedLimitedApps.value.toMutableList()
            if (limitedList.contains(packageName)) {
                limitedList.remove(packageName)
                _selectedLimitedApps.value = limitedList
            }
        } else {
            // Don't allow deselecting default apps
            if (packageName in defaultEssentialApps) {
                return
            }

            currentList.remove(packageName)
        }

        _selectedEssentialApps.value = currentList
    }

    fun toggleLimitedApp(packageName: String, selected: Boolean) {
        val currentList = _selectedLimitedApps.value.toMutableList()

        if (selected) {
            // Add to limited, remove from essential if present
            if (!currentList.contains(packageName)) {
                currentList.add(packageName)
            }

            val essentialList = _selectedEssentialApps.value.toMutableList()
            if (essentialList.contains(packageName)) {
                essentialList.remove(packageName)
                _selectedEssentialApps.value = essentialList
            }
        } else {
            // Don't allow deselecting default limited apps
            if (packageName in defaultLimitedApps) {
                return
            }

            currentList.remove(packageName)
        }

        _selectedLimitedApps.value = currentList
    }

    fun nextStep() {
        if (_onboardingStep.value < 3) {
            _onboardingStep.value += 1
        }
    }

    fun previousStep() {
        if (_onboardingStep.value > 1) {
            _onboardingStep.value -= 1
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val preferences = UserPreferences(
                essentialApps = _selectedEssentialApps.value,
                limitedApps = _selectedLimitedApps.value,
                onboardingCompleted = true
            )

            preferencesRepository.updatePreferences(preferences)
        }
    }
}