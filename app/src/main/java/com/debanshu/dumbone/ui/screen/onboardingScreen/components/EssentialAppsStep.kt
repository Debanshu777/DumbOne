package com.debanshu.dumbone.ui.screen.onboardingScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
                modifier = Modifier.weight(1f)
            )

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

                HorizontalDivider()
            }
        }
    }
}