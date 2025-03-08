package com.debanshu.dumbone.ui.screen.onboardingScreen.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debanshu.dumbone.data.model.AppInfo

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
                modifier = Modifier.weight(1f)
            )

            // Info button
            IconButton(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                onClick = { onInfoClick() }
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