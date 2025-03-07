package com.debanshu.dumbone.ui.screen


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    pagerState: PagerState? = null,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val usageStats by viewModel.usageStats.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val colors = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()

    // Get today's date as a string
    val today = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        .format(Date(System.currentTimeMillis()))

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button and refresh
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                MinimalIconButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    onClick = {
                        if (pagerState != null) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1) // Go to home page
                            }
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "Usage Statistics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )

                MinimalIconButton(
                    icon = Icons.Outlined.Refresh,
                    contentDescription = "Refresh",
                    onClick = { viewModel.refreshData() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            HorizontalDivider(color = colors.secondary.copy(alpha = 0.2f))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Date header
                item {
                    Text(
                        text = today,
                        fontSize = 16.sp,
                        color = colors.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }

                // Calculate total usage time
                item {
                    val totalUsageTime = usageStats.sumOf { it.totalUsageDuration }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Screen Time",
                            fontSize = 14.sp,
                            color = colors.secondary
                        )

                        Text(
                            text = formatDuration(totalUsageTime),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onBackground
                        )

                        Text(
                            text = "Apps Opened: ${usageStats.sumOf { it.usageCount }}",
                            fontSize = 14.sp,
                            color = colors.secondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    HorizontalDivider(color = colors.secondary.copy(alpha = 0.2f))
                }

                // If no usage data yet
                if (usageStats.isEmpty()) {
                    item {
                        Text(
                            text = "No usage data yet.\nStart using your apps to see statistics.",
                            fontSize = 16.sp,
                            color = colors.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                } else {
                    // App usage section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "App Usage")
                    }

                    // Sort by most used
                    val sortedStats = usageStats.sortedByDescending { it.usageCount }

                    items(sortedStats) { stat ->
                        val appName = allApps[stat.packageName]?.appName ?: "Unknown App"

                        UsageStatItem(
                            appName = appName,
                            usageCount = stat.usageCount,
                            usageDuration = stat.totalUsageDuration
                        )

                        HorizontalDivider(color = colors.secondary.copy(alpha = 0.1f))
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

    return when {
        hours > 0 -> "$hours h $minutes min"
        else -> "$minutes min"
    }
}