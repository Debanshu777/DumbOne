package com.debanshu.dumbone.ui.screen.onboardingScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.debanshu.dumbone.ui.screen.onboardingScreen.components.EssentialAppsStep
import com.debanshu.dumbone.ui.screen.onboardingScreen.components.LimitedAppsStep
import com.debanshu.dumbone.ui.screen.onboardingScreen.components.WelcomeStep

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
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
        )

        Text(
            text = "Step $step of 3",
            fontSize = 14.sp,
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
                        dialogContent =
                            "Essential apps are always available without restrictions. These are your most important tools for communication and productivity."
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
                        dialogContent =
                            "Limited apps use an exponential cooldown timer to help you break addictive usage patterns. Each time you use the app, the cooldown time increases:\n\n" +
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

    // Info Dialog
    if (showExplanationDialog) {
        Dialog(
            onDismissRequest = { showExplanationDialog = false }
        ) {
            Card(
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
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = dialogContent,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showExplanationDialog = false },
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
