package com.debanshu.dumbone.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val step by viewModel.onboardingStep.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val selectedEssentialApps by viewModel.selectedEssentialApps.collectAsState()
    val selectedLimitedApps by viewModel.selectedLimitedApps.collectAsState()
    val colors =  MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Progress dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) colors.primary else colors.secondary.copy(alpha = 0.3f)
                            )
                    )

                    if (i < 3) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }

            // Content based on step
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Step 1: Welcome
                AnimatedVisibility(
                    visible = step == 1,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Welcome to MinLauncher",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Take control of your digital life",
                            fontSize = 16.sp,
                            color = colors.secondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Text(
                            text = "MinLauncher helps you:\n\n" +
                                    "• Focus on what matters\n" +
                                    "• Reduce screen time\n" +
                                    "• Break addictive app habits\n" +
                                    "• Stay mindful of your usage",
                            fontSize = 16.sp,
                            color = colors.onBackground,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Step 2: Choose Essential Apps
                AnimatedVisibility(
                    visible = step == 2,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Select Essential Apps",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Choose up to 5 apps that will always be available",
                            fontSize = 14.sp,
                            color = colors.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Phone, Messages, and Chrome are pre-selected",
                                fontSize = 12.sp,
                                color = colors.secondary
                            )
                        }

                        Text(
                            text = "Selected: ${selectedEssentialApps.size}/5",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Divider(color = colors.secondary.copy(alpha = 0.2f))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val sortedApps = allApps.sortedBy { it.appName }

                            items(sortedApps) { app ->
                                val isSelected = selectedEssentialApps.contains(app.packageName)
                                val isPreSelected = app.packageName.contains("dialer") ||
                                        app.packageName.contains("messag") ||
                                        app.packageName.contains("chrome")

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.toggleEssentialApp(
                                                app.packageName,
                                                !isSelected
                                            )
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            viewModel.toggleEssentialApp(
                                                app.packageName,
                                                it
                                            )
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colors.primary,
                                            uncheckedColor = colors.secondary
                                        ),
                                        enabled = !isPreSelected || isSelected
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = app.appName,
                                        fontSize = 16.sp,
                                        color = colors.onBackground
                                    )

                                    if (isPreSelected) {
                                        Spacer(modifier = Modifier.weight(1f))

                                        Text(
                                            text = "(pre-selected)",
                                            fontSize = 12.sp,
                                            color = colors.secondary
                                        )
                                    }
                                }

                                Divider(color = colors.secondary.copy(alpha = 0.1f))
                            }
                        }
                    }
                }

                // Step 3: Choose Limited Access Apps
                AnimatedVisibility(
                    visible = step == 3,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Limited Access Apps",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Select apps that you want to limit",
                            fontSize = 14.sp,
                            color = colors.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Limited apps can be used, but after each use, " +
                                        "a cooldown timer will start.\n\n" +
                                        "The cooldown increases each time:\n" +
                                        "1st use: no cooldown\n" +
                                        "2nd use: 8 seconds\n" +
                                        "3rd use: 64 seconds\n" +
                                        "4th use: 512 seconds (8.5 min)\n" +
                                        "And so on...\n\n" +
                                        "Timers reset at midnight.",
                                fontSize = 14.sp,
                                color = colors.onSurface,
                                lineHeight = 20.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Social media apps are pre-selected",
                                fontSize = 12.sp,
                                color = colors.secondary
                            )
                        }

                        Divider(color = colors.secondary.copy(alpha = 0.2f))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Filter out essential apps
                            val availableApps = allApps
                                .filter { !selectedEssentialApps.contains(it.packageName) }
                                .sortedBy { it.appName }

                            items(availableApps) { app ->
                                val isSelected = selectedLimitedApps.contains(app.packageName)
                                val isPreSelected = app.packageName.contains("instagram") ||
                                        app.packageName.contains("facebook") ||
                                        app.packageName.contains("twitter") ||
                                        app.packageName.contains("tiktok") ||
                                        app.packageName.contains("snapchat")

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.toggleLimitedApp(
                                                app.packageName,
                                                !isSelected
                                            )
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            viewModel.toggleLimitedApp(
                                                app.packageName,
                                                it
                                            )
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colors.primary,
                                            uncheckedColor = colors.secondary
                                        ),
                                        enabled = !isPreSelected || isSelected
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = app.appName,
                                        fontSize = 16.sp,
                                        color = colors.onBackground
                                    )

                                    if (isPreSelected) {
                                        Spacer(modifier = Modifier.weight(1f))

                                        Text(
                                            text = "(pre-selected)",
                                            fontSize = 12.sp,
                                            color = colors.secondary
                                        )
                                    }
                                }

                                Divider(color = colors.secondary.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (step > 1) {
                    MinimalButton(
                        text = "Back",
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (step < 3) {
                    MinimalButton(
                        text = "Next",
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MinimalButton(
                        text = "Finish",
                        onClick = {
                            viewModel.completeOnboarding()
                            onComplete()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}