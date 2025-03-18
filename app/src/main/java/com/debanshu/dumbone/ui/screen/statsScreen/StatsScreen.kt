package com.debanshu.dumbone.ui.screen.statsScreen


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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.debanshu.dumbone.ui.common.formatDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val usageStats by viewModel.usageStats.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val needsPermission by viewModel.needsPermission.collectAsState()
    val colors = MaterialTheme.colorScheme
    val today = SimpleDateFormat(
        "EEEE, MMMM d",
        Locale.getDefault()
    ).format(Date(System.currentTimeMillis()))
    // Sort by most used
    val sortedStats = usageStats.sortedByDescending { it.usageCount }
    val maxUsageCount = sortedStats.maxOfOrNull { it.usageCount } ?: 1

    if (needsPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Usage Stats Permission Required",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DumbOne needs permission to access your app usage statistics to track how much time you spend on each app.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.requestUsageStatsPermission() },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Grant Permission")
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Calculate total usage time
                item {
                    val totalUsageTime = usageStats.sumOf { it.totalUsageDuration }
                    val totalOpenedCount = usageStats.sumOf { it.usageCount }

                    // Target time - for example, 2 hours max per day
                    val dailyTargetMillis = 2 * 60 * 60 * 1000L // 2 hours in milliseconds
                    val percentageOfTarget =
                        min(1f, totalUsageTime.toFloat() / dailyTargetMillis.toFloat())

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
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White.copy(alpha = 0.1f),
                                    strokeWidth = 8.dp
                                )

                                // Actual progress
                                CircularProgressIndicator(
                                    progress = { percentageOfTarget },
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
                                        text = totalUsageTime.formatDuration(),
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
                                        text = if (usageStats.isNotEmpty() && totalOpenedCount > 0) {
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
                    item(key = "app_usage") {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(
                            title = "App Usage",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(sortedStats) { stat ->
                        val appName = allApps[stat.packageName]?.appName ?: "Unknown App"
                        val usagePercentage = stat.usageCount.toFloat() / maxUsageCount.toFloat()

                        Card(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 4.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = appName,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "${stat.usageCount} opens",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { usagePercentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )

                                Text(
                                    text = "Total time: ${stat.totalUsageDuration.formatDuration()}",
                                    style = MaterialTheme.typography.bodyMedium
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

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}