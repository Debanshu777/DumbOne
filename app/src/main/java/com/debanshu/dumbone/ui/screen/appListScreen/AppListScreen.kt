package com.debanshu.dumbone.ui.screen.appListScreen


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.debanshu.dumbone.ui.common.noRippleClickable
import com.debanshu.dumbone.ui.common.onTriggerApp
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val essentialApps by viewModel.essentialApps.collectAsState()
    val limitedApps by viewModel.limitedApps.collectAsState()
    val appCooldowns by viewModel.appCooldowns.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("all") } // "all", "essential", "limited"

    // Filter apps based on search query and category
    val filteredEssentialApps = essentialApps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) &&
                (activeCategory == "all" || activeCategory == "essential")
    }

    val filteredLimitedApps = limitedApps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) &&
                (activeCategory == "all" || activeCategory == "limited")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .clipScrollableContainer(Orientation.Vertical)
    ) {
        // Search field
        stickyHeader {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search apps...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Text")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 4.dp),
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CategoryTab(
                    title = "All",
                    isSelected = activeCategory == "all",
                    onClick = { activeCategory = "all" },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                CategoryTab(
                    title = "Essential",
                    isSelected = activeCategory == "essential",
                    onClick = { activeCategory = "essential" },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                CategoryTab(
                    title = "Limited",
                    isSelected = activeCategory == "limited",
                    onClick = { activeCategory = "limited" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (appCooldowns.isNotEmpty() && (activeCategory == "all" || activeCategory == "limited")) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.1f
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text(
                                    text = "Active Cooldowns",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            appCooldowns.entries.sortedBy { it.value }
                                .forEach { (packageName, timeRemaining) ->
                                    val appName =
                                        limitedApps.find { it.packageName == packageName }?.appName
                                            ?: packageName
                                    val maxCooldownTime =
                                        8 * 60 * 1000L // Example max cooldown of 8 minutes
                                    val progress =
                                        1f - (timeRemaining.toFloat() / maxCooldownTime.toFloat()).coerceIn(
                                            0f,
                                            1f
                                        )

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                                alpha = 0.15f
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = appName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Visual countdown with CircularProgressIndicator
                                            Box(
                                                modifier = Modifier.size(64.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Background track
                                                CircularProgressIndicator(
                                                    progress = 1f,
                                                    modifier = Modifier.fillMaxSize(),
                                                    strokeWidth = 4.dp
                                                )

                                                // Actual progress
                                                CircularProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier.fillMaxSize(),
                                                    strokeWidth = 4.dp,
                                                    strokeCap = StrokeCap.Round
                                                )

                                                // Time remaining in the center
                                                Text(
                                                    text = formatTime(timeRemaining),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }

        // Essential apps section
        if (filteredEssentialApps.isNotEmpty()) {
            item {
                if (activeCategory != "essential") {
                    Text(
                        text = "Essential Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    HorizontalDivider()
                }
            }

            items(filteredEssentialApps) { app ->
                AppItemCard(
                    name = app.appName,
                    onClick = {
                        context.onTriggerApp(app, viewModel::launchApp)
                    },
                    isEssential = true
                )
            }
        }

        // Limited apps section
        if (filteredLimitedApps.isNotEmpty()) {
            item {
                if (activeCategory != "limited") {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Limited Access Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    HorizontalDivider()
                }
            }

            items(filteredLimitedApps) { app ->
                val isInCooldown = viewModel.isAppInCooldown(app.packageName)
                val cooldownTime = viewModel.getCooldownTimeRemaining(app.packageName)

                AppItemCard(
                    name = app.appName,
                    onClick = {
                        context.onTriggerApp(app, viewModel::launchApp)
                    },
                    isEssential = false,
                    isInCooldown = isInCooldown,
                    cooldownTimeRemaining = cooldownTime
                )
            }
        }

        // Empty state if no apps match the filter
        if (filteredEssentialApps.isEmpty() && filteredLimitedApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps found matching \"$searchQuery\"",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animateContainerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainer
    )
    val animateTextColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface
    )
    Card(
        modifier = modifier
            .noRippleClickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = animateContainerColor
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = animateTextColor,
                style = if (isSelected)
                    MaterialTheme.typography.titleSmall
                else
                    MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AppItemCard(
    name: String,
    onClick: () -> Unit,
    isEssential: Boolean,
    isInCooldown: Boolean = false,
    cooldownTimeRemaining: Long = 0
) {
    val cardColor = when {
        isInCooldown -> MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
        isEssential -> MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
    }

    val textColor = when {
        isInCooldown -> Color.White.copy(alpha = 0.5f)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(enabled = !isInCooldown) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isInCooldown && cooldownTimeRemaining > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Available in: ${formatTime(cooldownTimeRemaining)}",
                        fontSize = 12.sp,
                    )
                }
            }

            // Visual indicator for app type
            if (isEssential) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else if (isInCooldown) {
                // Simplified cooldown indicator - just shows time remaining
                Text(
                    text = formatTime(cooldownTimeRemaining),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// Utility function to format time
fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return when {
        minutes > 0 -> "$minutes:${seconds.toString().padStart(2, '0')}"
        else -> "$seconds s"
    }
}