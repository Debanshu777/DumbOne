package com.debanshu.dumbone.ui.screen.onboardingScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.debanshu.dumbone.data.model.AppInfo

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val step by viewModel.onboardingStep.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val selectedEssentialApps by viewModel.selectedEssentialApps.collectAsState()
    val selectedLimitedApps by viewModel.selectedLimitedApps.collectAsState()

    // Dialog state for info dialogs
    var showExplanationDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogContent by remember { mutableStateOf("") }

    // Create gradient background
    val gradientColors = listOf(
        Color(0xFF121212),  // Dark background base
        Color(0xFF1F1F1F)   // Slightly lighter shade at bottom
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Progress indicator
                val progress = (step - 1) / 2f  // 3 steps, 0 to 1 range

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Text(
                    text = "Step $step of 3",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Content based on step with animations
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { width -> width } togetherWith
                                slideOutHorizontally { width -> -width }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = "Step Animation"
                ) { currentStep ->
                    when (currentStep) {
                        // Step 1: Welcome
                        1 -> WelcomeStep()

                        // Step 2: Choose Essential Apps
                        2 -> EssentialAppsStep(
                            allApps = allApps,
                            selectedEssentialApps = selectedEssentialApps,
                            toggleEssentialApp = viewModel::toggleEssentialApp,
                            onInfoClick = {
                                dialogTitle = "Essential Apps"
                                dialogContent = "Essential apps are always available without restrictions. These are your most important tools for communication and productivity."
                                showExplanationDialog = true
                            }
                        )

                        // Step 3: Choose Limited Access Apps
                        3 -> LimitedAppsStep(
                            allApps = allApps.filter { !selectedEssentialApps.contains(it.packageName) },
                            selectedLimitedApps = selectedLimitedApps,
                            toggleLimitedApp = viewModel::toggleLimitedApp,
                            onInfoClick = {
                                dialogTitle = "Limited Access Apps"
                                dialogContent = "Limited apps use an exponential cooldown timer to help you break addictive usage patterns. Each time you use the app, the cooldown time increases:\n\n" +
                                        "1st use: no cooldown\n" +
                                        "2nd use: 8 seconds\n" +
                                        "3rd use: 64 seconds (1 min)\n" +
                                        "4th use: 512 seconds (8.5 min)\n" +
                                        "5th use: 4096 seconds (68 min)\n\n" +
                                        "All timers reset at midnight."
                                showExplanationDialog = true
                            }
                        )
                    }
                }

                // Navigation buttons with improved styling
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (step > 1) {
                        Button(
                            onClick = { viewModel.previousStep() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (step < 3) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Next",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.completeOnboarding()
                                onComplete()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Finish",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Info Dialog
        if (showExplanationDialog) {
            Dialog(
                onDismissRequest = { showExplanationDialog = false }
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF282828)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = dialogTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = dialogContent,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showExplanationDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Got it")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App logo placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DumbOne",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to DumbOne",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Take control of your digital life",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Benefits list with improved styling
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BenefitItem(
                    title = "Focus",
                    description = "Eliminate distractions and stay on task"
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                BenefitItem(
                    title = "Mindfulness",
                    description = "Be intentional about your screen time"
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                BenefitItem(
                    title = "Balance",
                    description = "Build healthier digital habits"
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                BenefitItem(
                    title = "Control",
                    description = "Break free from addictive app cycles"
                )
            }
        }
    }
}

@Composable
fun BenefitItem(title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EssentialAppsStep(
    allApps: List<AppInfo>,
    selectedEssentialApps: List<String>,
    toggleEssentialApp: (String, Boolean) -> Unit,
    onInfoClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Essential Apps",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            // Info button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onInfoClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose up to 5 apps that will always be available",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selected count indicator
        val selectedCount = selectedEssentialApps.size
        val isMaxReached = selectedCount >= 5

        Text(
            text = "Selected: $selectedCount/5",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isMaxReached) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Divider(color = Color.White.copy(alpha = 0.1f))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(allApps.sortedBy { it.appName }) { app ->
                val isSelected = selectedEssentialApps.contains(app.packageName)
                val isPreSelected = app.packageName.contains("dialer") ||
                        app.packageName.contains("messag") ||
                        app.packageName.contains("chrome")
                val isDisabled = !isSelected && isMaxReached && !isPreSelected

                AppSelectionItem(
                    appName = app.appName,
                    isSelected = isSelected,
                    isDisabled = isDisabled,
                    isPreSelected = isPreSelected,
                    onToggle = { selected ->
                        toggleEssentialApp(app.packageName, selected)
                    }
                )

                Divider(color = Color.White.copy(alpha = 0.05f))
            }
        }
    }
}

@Composable
fun LimitedAppsStep(
    allApps: List<AppInfo>,
    selectedLimitedApps: List<String>,
    toggleLimitedApp: (String, Boolean) -> Unit,
    onInfoClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Limited Access Apps",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            // Info button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onInfoClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select apps you want to use mindfully",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cooldown explanation card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How Cooldown Works",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "After each use, a cooldown timer starts. The wait time increases with each use, encouraging mindful usage.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Visual timeline of cooldown progression
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimelineItem(number = 1, time = "0s")
                    TimelineItem(number = 2, time = "8s")
                    TimelineItem(number = 3, time = "64s")
                    TimelineItem(number = 4, time = "8.5m")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(color = Color.White.copy(alpha = 0.1f))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Filter out pre-selected social apps for better highlighting
            val socialApps = allApps.filter { app ->
                app.packageName.contains("instagram") ||
                        app.packageName.contains("facebook") ||
                        app.packageName.contains("twitter") ||
                        app.packageName.contains("tiktok") ||
                        app.packageName.contains("snapchat")
            }.sortedBy { it.appName }

            val otherApps = allApps.filter { app ->
                !socialApps.contains(app)
            }.sortedBy { it.appName }

            if (socialApps.isNotEmpty()) {
                item {
                    Text(
                        text = "Social Media",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }

                items(socialApps) { app ->
                    val isSelected = selectedLimitedApps.contains(app.packageName)
                    val isPreSelected = app.packageName.contains("instagram") ||
                            app.packageName.contains("facebook") ||
                            app.packageName.contains("twitter") ||
                            app.packageName.contains("tiktok") ||
                            app.packageName.contains("snapchat")

                    AppSelectionItem(
                        appName = app.appName,
                        isSelected = isSelected,
                        isDisabled = false,
                        isPreSelected = isPreSelected,
                        onToggle = { selected ->
                            toggleLimitedApp(app.packageName, selected)
                        },
                        showRecommendedTag = isPreSelected
                    )

                    Divider(color = Color.White.copy(alpha = 0.05f))
                }

                item {
                    Text(
                        text = "Other Apps",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }
            }

            items(otherApps) { app ->
                val isSelected = selectedLimitedApps.contains(app.packageName)

                AppSelectionItem(
                    appName = app.appName,
                    isSelected = isSelected,
                    isDisabled = false,
                    isPreSelected = false,
                    onToggle = { selected ->
                        toggleLimitedApp(app.packageName, selected)
                    }
                )

                Divider(color = Color.White.copy(alpha = 0.05f))
            }
        }
    }
}

@Composable
fun TimelineItem(number: Int, time: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle with number
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = time,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AppSelectionItem(
    appName: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    isPreSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    showRecommendedTag: Boolean = false
) {
    val itemColor = when {
        isDisabled -> Color.White.copy(alpha = 0.3f)
        isSelected -> Color.White
        else -> Color.White.copy(alpha = 0.7f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled) {
                if (!isPreSelected || isSelected) {
                    onToggle(!isSelected)
                }
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = {
                if (!isDisabled && (!isPreSelected || isSelected)) {
                    onToggle(it)
                }
            },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = Color.White.copy(alpha = 0.5f),
                disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledUncheckedColor = Color.White.copy(alpha = 0.2f)
            ),
            enabled = !isDisabled || isSelected
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = appName,
            fontSize = 16.sp,
            color = itemColor
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isPreSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (showRecommendedTag) "Recommended" else "Pre-selected",
                    fontSize = 12.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}