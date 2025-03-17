package com.debanshu.dumbone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.debanshu.dumbone.ui.screen.appListScreen.AppListScreen
import com.debanshu.dumbone.ui.screen.homeScreen.HomeScreen
import com.debanshu.dumbone.ui.screen.homeScreen.HomeViewModel
import com.debanshu.dumbone.ui.screen.onboardingScreen.OnboardingScreen
import com.debanshu.dumbone.ui.screen.onboardingScreen.OnboardingViewModel
import com.debanshu.dumbone.ui.screen.statsScreen.StatsScreen
import com.debanshu.dumbone.ui.theme.DumbOneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DumbOneTheme {
                val homeViewModel = hiltViewModel<HomeViewModel>()
                val onboardingViewModel = hiltViewModel<OnboardingViewModel>()
                val isOnboardingCompleted by homeViewModel.isOnboardingCompleted.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = onboardingViewModel,
                            onComplete = { }
                        )
                    } else {
                        val pagerState = rememberPagerState(initialPage = 1) { 3 }
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            userScrollEnabled = true,
                            pageSpacing = 0.dp
                        ) { page ->
                            when (page) {
                                0 -> StatsScreen()
                                1 -> HomeScreen()
                                2 -> AppListScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}