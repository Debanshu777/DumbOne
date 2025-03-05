package com.debanshu.dumbone.ui.screen


import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debanshu.dumbone.ui.theme.DumbOneTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAppList: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current
    val currentTime by viewModel.currentTime.collectAsState()
    val essentialApps by viewModel.essentialApps.collectAsState()
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Swipe indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "< Stats",
                    fontSize = 14.sp,
                    color = colors.secondary,
                    modifier = Modifier.padding(8.dp)
                )

                Text(
                    text = "Apps >",
                    fontSize = 14.sp,
                    color = colors.secondary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Clock display
                ClockDisplay(
                    currentTime = currentTime,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Essential app shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Phone app shortcut
                    MinimalIconButton(
                        icon = Icons.Outlined.Call,
                        contentDescription = "Phone",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL)
                            context.startActivity(intent)

                            // Find the phone app package name and record usage
                            essentialApps.find {
                                it.packageName.contains("dialer") ||
                                        it.packageName.contains("phone")
                            }?.let {
                                viewModel.launchApp(it.packageName)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    // Messages app shortcut
                    MinimalIconButton(
                        icon = Icons.Outlined.Message,
                        contentDescription = "Messages",
                        onClick = {
                            val intent = Intent(Intent.ACTION_MAIN)
                            intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
                            context.startActivity(intent)

                            // Find the messaging app package name and record usage
                            essentialApps.find {
                                it.packageName.contains("messag") ||
                                        it.packageName.contains("mms") ||
                                        it.packageName.contains("sms")
                            }?.let {
                                viewModel.launchApp(it.packageName)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    // App drawer button
                    MinimalIconButton(
                        icon = Icons.Outlined.Apps,
                        contentDescription = "Apps",
                        onClick = onNavigateToAppList
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    // Stats button
                    MinimalIconButton(
                        icon = Icons.Outlined.QueryStats,
                        contentDescription = "Stats",
                        onClick = onNavigateToStats
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Minimalist intent - reminder of what this launcher is for
                Text(
                    text = "Focus on what matters.",
                    fontSize = 16.sp,
                    color = colors.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}