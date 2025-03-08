package com.debanshu.dumbone.ui.screen.statsScreen


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val usageStats by viewModel.usageStats.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val colors = MaterialTheme.colorScheme

    // Get today's date as a string
    val today = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        .format(Date(System.currentTimeMillis()))


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date header
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = today,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }

                // Calculate total usage time
                item {
                    val totalUsageTime = usageStats.sumOf { it.totalUsageDuration }
                    val totalOpenedCount = usageStats.sumOf { it.usageCount }

                    // Target time - for example, 2 hours max per day
                    val dailyTargetMillis = 2 * 60 * 60 * 1000L // 2 hours in milliseconds
                    val percentageOfTarget = min(1f, totalUsageTime.toFloat() / dailyTargetMillis.toFloat())

                    // Get the progress indicator color based on usage percentage
                    val progressColor = when {
                        percentageOfTarget < 0.5f -> Color(0xFF4CAF50) // Green
                        percentageOfTarget < 0.75f -> Color(0xFFFFC107) // Amber
                        else -> Color(0xFFF44336) // Red
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surface.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Daily Screen Time",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Circular progress indicator
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background track
                                CircularProgressIndicator(
                                    progress = 1f,
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White.copy(alpha = 0.1f),
                                    strokeWidth = 8.dp
                                )

                                // Actual progress
                                CircularProgressIndicator(
                                    progress = percentageOfTarget,
                                    modifier = Modifier.fillMaxSize(),
                                    color = progressColor,
                                    strokeWidth = 8.dp,
                                    strokeCap = StrokeCap.Round
                                )

                                // Display the time in the center
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = formatDuration(totalUsageTime),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = totalOpenedCount.toString(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Apps Opened",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (usageStats.isNotEmpty()) {
                                            (totalUsageTime / totalOpenedCount / 1000 / 60).toString() + "m"
                                        } else "0m",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Avg Session",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // If no usage data yet
                if (usageStats.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surface.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "No usage data yet.\nStart using your apps to see statistics.",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                        }
                    }
                } else {
                    // App usage section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "App Usage",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider(color = colors.onSurface.copy(alpha = 0.1f))
                    }

                    // Sort by most used
                    val sortedStats = usageStats.sortedByDescending { it.usageCount }
                    val maxUsageCount = sortedStats.maxOfOrNull { it.usageCount } ?: 1

                    items(sortedStats) { stat ->
                        val appName = allApps[stat.packageName]?.appName ?: "Unknown App"
                        val usagePercentage = stat.usageCount.toFloat() / maxUsageCount.toFloat()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surface.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = appName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "${stat.usageCount} opens",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Usage progress bar
                                LinearProgressIndicator(
                                    progress = usagePercentage,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Total time: ${formatDuration(stat.totalUsageDuration)}",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
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

// Helper function to render a simple bar chart for usage patterns
@Composable
fun UsageBarChart(
    usageData: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxValue = usageData.maxOfOrNull { it.second } ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Usage Patterns",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val barWidth = size.width / usageData.size

                usageData.forEachIndexed { index, (label, value) ->
                    val barHeight = (value / maxValue.toFloat()) * size.height

                    // Draw the bar - using a non-Composable method that works in Canvas
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(index * barWidth, size.height - barHeight),
                        size = Size(barWidth * 0.8f, barHeight)
                    )
                }
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                usageData.forEach { (label, _) ->
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
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