package com.debanshu.dumbone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                val isOnboardingCompleted by homeViewModel.isOnboardingCompleted.collectAsState()

                if (!isOnboardingCompleted) {
                    // Show onboarding if not completed
                    val onboardingViewModel = hiltViewModel<OnboardingViewModel>()
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onComplete = { /* Onboarding completed */ }
                    )
                } else {
                    // Main launcher with pager
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val pagerState = rememberPagerState(initialPage = 1) { 3 }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = true, // Enable user scrolling
                            pageSpacing = 0.dp // No spacing between pages for seamless swiping
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