package com.debanshu.dumbone.ui.screen.statsScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.debanshu.dumbone.data.model.AppCategory
import com.debanshu.dumbone.data.model.DailyUsageSummary
import com.debanshu.dumbone.data.model.HourlyUsage
import com.debanshu.dumbone.data.model.formatDurationForDisplay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val needsPermission by viewModel.needsPermission.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedViewMode by viewModel.selectedViewMode.collectAsState()
    val useRealData by viewModel.useRealData.collectAsState()

    val dailyScreenTime by viewModel.dailyScreenTime.collectAsState()
    val appOpenCount by viewModel.appOpenCount.collectAsState()
    val notificationCount by viewModel.notificationCount.collectAsState()
    val unlockCount by viewModel.unlockCount.collectAsState()
    val productivityScore by viewModel.productivityScore.collectAsState()

    val hourlyData by viewModel.hourlyData.collectAsState()
    val contributionData by viewModel.contributionData.collectAsState()
    val appUsageSummary by viewModel.appUsageSummary.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val formattedDate = remember(selectedDate) {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(selectedDate))
    }

    val isToday = remember(selectedDate) {
        val today = Calendar.getInstance()
        val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }

        today.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)
    }

    if (needsPermission) {
        PermissionRequestScreen(
            onRequestPermission = { viewModel.requestUsageStatsPermission() }
        )
    } else {
        Scaffold(
            topBar = {
                StatsTopBar(
                    title = "Digital Wellbeing",
                    date = formattedDate,
                    isToday = isToday,
                    useRealData = useRealData,
                    onToggleDataMode = {
                        viewModel.toggleDataMode()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Using ${if (useRealData) "real" else "simulated"} data"
                            )
                        }
                    },
                    onRefresh = { viewModel.refreshData() }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (isLoading) {
                LoadingScreen()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // View mode selector
                    item {
                        ViewModeSelector(
                            selectedMode = selectedViewMode,
                            onModeSelected = { viewModel.setViewMode(it) }
                        )
                    }

                    // Selected view content
                    item {
                        AnimatedContent(
                            targetState = selectedViewMode,
                            label = "ViewModeTransition"
                        ) { mode ->
                            when (mode) {
                                0 -> ContributionView(
                                    contributionData = contributionData,
                                    selectedDate = selectedDate,
                                    onDateSelected = { viewModel.setSelectedDate(it) }
                                )

                                1 -> CircularStatsView(
                                    screenTime = dailyScreenTime,
                                    appOpenCount = appOpenCount,
                                    notificationCount = notificationCount,
                                    unlockCount = unlockCount,
                                    productivityScore = productivityScore,
                                    categoryStats = categoryStats
                                )

                                2 -> HourlyStatsView(hourlyData = hourlyData)
                            }
                        }
                    }

                    // Key stats
                    item {
                        KeyStatsRow(
                            screenTime = dailyScreenTime,
                            appOpenCount = appOpenCount,
                            notificationCount = notificationCount,
                            unlockCount = unlockCount
                        )
                    }

                    // Productivity score
                    item {
                        ProductivityScoreCard(
                            score = productivityScore
                        )
                    }

                    // App usage section
                    item {
                        SectionHeader(
                            title = "App Usage",
                            subtitle = "Most used apps today"
                        )
                    }

                    // App usage list
                    items(appUsageSummary) { appSummary ->
                        AppUsageItem(
                            appName = appSummary.appName,
                            usageTime = appSummary.usageTime,
                            openCount = appSummary.openCount,
                            isProductiveApp = appSummary.isProductiveApp,
                            category = appSummary.category
                        )
                    }

                    // Categories section
                    item {
                        SectionHeader(
                            title = "Usage by Category",
                            subtitle = "How you spend your time"
                        )
                    }

                    // Category bar chart
                    item {
                        CategoryBarChart(
                            categoryStats = categoryStats,
                            totalTime = dailyScreenTime
                        )
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

@Composable
private fun StatsTopBar(
    title: String,
    date: String,
    isToday: Boolean,
    useRealData: Boolean,
    onToggleDataMode: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Developer mode switch
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle between real and simulated data
                FilterChip(
                    selected = useRealData,
                    onClick = onToggleDataMode,
                    label = { Text(if (useRealData) "Real Data" else "Simulated Data") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun KeyStatsRow(
    screenTime: Long,
    appOpenCount: Int,
    notificationCount: Int,
    unlockCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KeyStatItem(
            icon = Icons.Outlined.Schedule,
            value = screenTime.formatDurationForDisplay(),
            label = "Screen Time",
            modifier = Modifier.weight(1f)
        )

        KeyStatItem(
            icon = Icons.Outlined.TouchApp,
            value = appOpenCount.toString(),
            label = "App Opens",
            modifier = Modifier.weight(1f)
        )

        KeyStatItem(
            icon = Icons.Outlined.Notifications,
            value = notificationCount.toString(),
            label = "Notifications",
            modifier = Modifier.weight(1f)
        )

        KeyStatItem(
            icon = Icons.Outlined.PhoneAndroid,
            value = unlockCount.toString(),
            label = "Unlocks",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KeyStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ProductivityScoreCard(
    score: Float
) {
    val productivityColor = remember(score) {
        when {
            score < 0.3f -> Color(0xFFF44336) // Red
            score < 0.5f -> Color(0xFFFF9800) // Orange
            score < 0.7f -> Color(0xFFFFEB3B) // Yellow
            score < 0.9f -> Color(0xFF8BC34A) // Light Green
            else -> Color(0xFF4CAF50) // Green
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = score,
        label = "ProductivityAnimation"
    )

    val productivityDesc = remember(score) {
        when {
            score < 0.3f -> "Poor"
            score < 0.5f -> "Fair"
            score < 0.7f -> "Good"
            score < 0.9f -> "Very Good"
            else -> "Excellent"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productivity Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = productivityDesc,
                        color = productivityColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = productivityColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(score * 100).toInt()}% - Based on how you use your apps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AppUsageItem(
    appName: String,
    usageTime: Long,
    openCount: Int,
    isProductiveApp: Boolean,
    category: AppCategory
) {
    val backgroundColor =
        if (isProductiveApp) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = category.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = usageTime.formatDurationForDisplay(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$openCount opens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CategoryBarChart(
    categoryStats: Map<AppCategory, Long>,
    totalTime: Long
) {
    if (categoryStats.isEmpty()) {
        return
    }

    val sortedCategories = remember(categoryStats) {
        categoryStats.entries.sortedByDescending { it.value }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            sortedCategories.forEach { (category, time) ->
                val percentage = if (totalTime > 0) {
                    time.toFloat() / totalTime.toFloat()
                } else 0f

                val animatedProgress by animateFloatAsState(
                    targetValue = percentage,
                    label = "CategoryBarAnimation"
                )

                val color = when (category) {
                    AppCategory.PRODUCTIVITY -> Color(0xFF4CAF50) // Green
                    AppCategory.COMMUNICATION -> Color(0xFF2196F3) // Blue
                    AppCategory.UTILITY -> Color(0xFF9C27B0) // Purple
                    AppCategory.SOCIAL -> Color(0xFFF44336) // Red
                    AppCategory.ENTERTAINMENT -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFF607D8B) // Gray
                }

                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.getDisplayName(),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = time.formatDurationForDisplay(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${(percentage * 100).toInt()}% of total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyStatsView(
    hourlyData: List<HourlyUsage>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hourly Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Convert to map for easier lookup
            val hourlyDataMap = hourlyData.associateBy { it.hour }

            // Find max value for scaling
            val maxScreenTime = hourlyData.maxOfOrNull { it.screenTime } ?: 0L

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Horizontal grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridLineColor = Color.White.copy(alpha = 0.1f)
                    val lineCount = 3

                    for (i in 0..lineCount) {
                        val y = size.height * i / lineCount
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Bars
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp), // Space for labels
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (hour in 0..23) {
                        val data = hourlyDataMap[hour]

                        val screenTime = data?.screenTime ?: 0L
                        val productiveTime = data?.productiveTime ?: 0L
                        val distractingTime = data?.distractingTime ?: 0L

                        val barHeightPercent = if (maxScreenTime > 0)
                            screenTime.toFloat() / maxScreenTime.toFloat() else 0f

                        val productivePercent = if (screenTime > 0)
                            productiveTime.toFloat() / screenTime.toFloat() else 0f

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (screenTime > 0) {
                                // Productive bar (green)
                                if (productiveTime > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .fillMaxHeight(barHeightPercent * productivePercent)
                                            .background(
                                                Color(0xFF4CAF50),
                                                RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                            )
                                    )
                                }

                                // Distracting bar (orange/red)
                                if (distractingTime > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .fillMaxHeight(barHeightPercent * (1 - productivePercent))
                                            .background(
                                                Color(0xFFF44336),
                                                RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                            )
                                    )
                                }
                            }

                            // Display hour labels consistently every 3 hours
                            if (hour % 3 == 0) {
                                Text(
                                    text = formatHour(hour),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Productive",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFF44336), CircleShape)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Distracting",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Helper to format hour labels properly
private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12am"
        hour < 12 -> "${hour}am"
        hour == 12 -> "12pm"
        else -> "${hour - 12}pm"
    }
}

@Composable
private fun ContributionView(
    contributionData: Map<Long, DailyUsageSummary>,
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    // Organize data by weeks and days
    val (weeks, daysOfWeek) = remember(contributionData) {
        val weeks = mutableListOf<List<Pair<Long, DailyUsageSummary?>>>()
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        // Get first day of current week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        // Create 12 weeks (3 months) of data
        for (weekIndex in 0 until 12) {
            val week = mutableListOf<Pair<Long, DailyUsageSummary?>>()

            // For each day in the week
            for (dayIndex in 0 until 7) {
                val timestamp = calendar.timeInMillis
                val summary = contributionData[timestamp]

                // Only include days up to today
                if (calendar.timeInMillis <= today.timeInMillis) {
                    week.add(Pair(timestamp, summary))
                }

                // Move to next day
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            if (week.isNotEmpty()) {
                weeks.add(0, week) // Add to beginning of list to show most recent first
            }
        }

        // Get day names
        val dayNames = (0 until 7).map {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek + it)
            SimpleDateFormat("E", Locale.getDefault()).format(cal.time)[0].toString()
        }

        Pair(weeks, dayNames)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Activity History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Days of week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contribution grid
            weeks.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { (timestamp, summary) ->
                        val isSelected = timestamp == selectedDate
                        val color = when {
                            summary == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            summary.productivityScore < 0.3f -> Color(0xFFF44336) // Red
                            summary.productivityScore < 0.5f -> Color(0xFFFF9800) // Orange
                            summary.productivityScore < 0.7f -> Color(0xFFFFEB3B) // Yellow
                            summary.productivityScore < 0.9f -> Color(0xFF8BC34A) // Light Green
                            else -> Color(0xFF4CAF50) // Green
                        }

                        val boxColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                color.copy(alpha = 1f)
                            } else {
                                color.copy(alpha = if (summary != null) 0.7f else 0.4f)
                            }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(boxColor)
                                .clickable(enabled = summary != null) {
                                    onDateSelected(timestamp)
                                }
                                .padding(1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(0xFFF44336).copy(alpha = 0.7f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(0xFFFF9800).copy(alpha = 0.7f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(0xFFFFEB3B).copy(alpha = 0.7f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(0xFF8BC34A).copy(alpha = 0.7f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(0xFF4CAF50).copy(alpha = 0.7f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }

                Text(
                    text = "More",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CircularStatsView(
    screenTime: Long,
    appOpenCount: Int,
    notificationCount: Int,
    unlockCount: Int,
    productivityScore: Float,
    categoryStats: Map<AppCategory, Long>
) {
    val color = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen time circle
            val animatedProgress by animateFloatAsState(
                targetValue = productivityScore,
                label = "ProductivityAnimation"
            )

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background track
                    drawArc(
                        color = color.surfaceVariant,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Calculate category sweepAngles
                    if (categoryStats.isNotEmpty()) {
                        val totalTime = categoryStats.values.sum()
                        var startAngle = 0f

                        // Draw arcs for each category
                        categoryStats.forEach { (category, time) ->
                            val sweepAngle = (time.toFloat() / totalTime.toFloat()) * 360f

                            val color = when (category) {
                                AppCategory.PRODUCTIVITY -> Color(0xFF4CAF50) // Green
                                AppCategory.COMMUNICATION -> Color(0xFF2196F3) // Blue
                                AppCategory.UTILITY -> Color(0xFF9C27B0) // Purple
                                AppCategory.SOCIAL -> Color(0xFFF44336) // Red
                                AppCategory.ENTERTAINMENT -> Color(0xFFFF9800) // Orange
                                else -> Color(0xFF607D8B) // Gray
                            }

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )

                            startAngle += sweepAngle
                        }
                    } else {
                        // If no category data, show progress indicator
                        val progressColor = when {
                            animatedProgress < 0.3f -> Color(0xFFF44336) // Red
                            animatedProgress < 0.5f -> Color(0xFFFF9800) // Orange
                            animatedProgress < 0.7f -> Color(0xFFFFEB3B) // Yellow
                            animatedProgress < 0.9f -> Color(0xFF8BC34A) // Light Green
                            else -> Color(0xFF4CAF50) // Green
                        }

                        drawArc(
                            color = progressColor,
                            startAngle = 270f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = screenTime.formatDurationForDisplay(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Screen time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryStats.forEach { (category, time) ->
                    val color = when (category) {
                        AppCategory.PRODUCTIVITY -> Color(0xFF4CAF50) // Green
                        AppCategory.COMMUNICATION -> Color(0xFF2196F3) // Blue
                        AppCategory.UTILITY -> Color(0xFF9C27B0) // Purple
                        AppCategory.SOCIAL -> Color(0xFFF44336) // Red
                        AppCategory.ENTERTAINMENT -> Color(0xFFFF9800) // Orange
                        else -> Color(0xFF607D8B) // Gray
                    }

                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${category.getDisplayName()}: ${time.formatDurationForDisplay()}"
                            )
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, CircleShape)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Usage Stats Permission Required",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DumbOne needs permission to access your app usage statistics to track how much time you spend on each app.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ViewModeSelector(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selectedMode == 0,
            onClick = { onModeSelected(0) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            icon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        ) {
            Text("Calendar")
        }

        SegmentedButton(
            selected = selectedMode == 1,
            onClick = { onModeSelected(1) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            icon = {
                Icon(
                    imageVector = Icons.Default.DonutLarge,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        ) {
            Text("Overview")
        }

        SegmentedButton(
            selected = selectedMode == 2,
            onClick = { onModeSelected(2) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            icon = {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        ) {
            Text("Hourly")
        }
    }
}