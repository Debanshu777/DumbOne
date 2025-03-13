package com.debanshu.dumbone.ui.screen.appListScreen.model

import com.debanshu.dumbone.data.model.AppInfo

data class AppListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val activeCategory: AppCategory = AppCategory.ALL,
    val essentialApps: List<AppInfo> = emptyList(),
    val limitedApps: List<AppWithCooldown> = emptyList(),
    val hasResults: Boolean = true
)
