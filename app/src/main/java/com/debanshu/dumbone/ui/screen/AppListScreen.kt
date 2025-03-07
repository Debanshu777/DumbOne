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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListScreen(
    onNavigateBack: () -> Unit,
    pagerState: PagerState? = null,
    viewModel: AppListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val essentialApps by viewModel.essentialApps.collectAsState()
    val limitedApps by viewModel.limitedApps.collectAsState()
    val appCooldowns by viewModel.appCooldowns.collectAsState()
    val colors = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button
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
                    text = "Applications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            HorizontalDivider(color = colors.secondary.copy(alpha = 0.2f))

            // App lists
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Essential apps section
                item {
                    SectionHeader(title = "Essential Apps")
                }

                items(essentialApps) { app ->
                    AppItem(
                        name = app.appName,
                        onClick = {
                            try {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) {
                                    viewModel.launchApp(app.packageName)
                                    context.startActivity(launchIntent)
                                }
                            } catch (e: Exception) {
                                // Handle exception
                            }
                        }
                    )
                    HorizontalDivider(color = colors.secondary.copy(alpha = 0.1f))
                }

                // Limited apps section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Limited Access Apps")
                }

                items(limitedApps) { app ->
                    val isInCooldown = viewModel.isAppInCooldown(app.packageName)
                    val cooldownTime = viewModel.getCooldownTimeRemaining(app.packageName)

                    AppItem(
                        name = app.appName,
                        onClick = {
                            try {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) {
                                    viewModel.launchApp(app.packageName)
                                    context.startActivity(launchIntent)
                                }
                            } catch (e: Exception) {
                                // Handle exception
                            }
                        },
                        isInCooldown = isInCooldown,
                        cooldownTimeRemaining = cooldownTime
                    )
                    HorizontalDivider(color = colors.secondary.copy(alpha = 0.1f))
                }

                // Show active cooldown timers at the top for better visibility
                if (appCooldowns.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Active Cooldowns",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onBackground
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                appCooldowns.entries.sortedBy { it.value }.forEach { (packageName, timeRemaining) ->
                                    val appName = limitedApps.find { it.packageName == packageName }?.appName ?: packageName

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = appName,
                                            fontSize = 16.sp,
                                            color = colors.onBackground
                                        )

                                        CooldownTimer(
                                            timeRemaining = timeRemaining,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )

                                        HorizontalDivider(color = colors.secondary.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}