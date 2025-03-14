package com.debanshu.dumbone.ui.screen.appListScreen

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.debanshu.dumbone.ui.common.formatTime
import com.debanshu.dumbone.ui.common.noRippleClickable
import com.debanshu.dumbone.ui.common.onTriggerApp
import com.debanshu.dumbone.ui.screen.appListScreen.model.AppCategory
import com.debanshu.dumbone.ui.screen.appListScreen.model.AppWithCooldown

/**
 * Main screen for displaying app list with categories for essential and limited apps.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppListScreen(
    viewModel: AppListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search field
        stickyHeader {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
            )
        }

        // Category tabs
        item {
            CategoryTabs(
                selectedCategory = uiState.activeCategory,
                onCategorySelected = viewModel::onCategorySelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Loading state
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            // Essential apps section
            if (uiState.essentialApps.isNotEmpty()) {
                item {
                    if (uiState.activeCategory != AppCategory.ESSENTIAL) {
                        SectionHeader(
                            title = "Essential Apps",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(
                    items = uiState.essentialApps,
                    key = { it.appName + it.packageName }
                ) { app ->
                    AppItemCard(
                        name = app.appName,
                        icon = app.icon,
                        onClick = {
                            context.onTriggerApp(app, viewModel::launchApp)
                        },
                        accentColor = app.dominantColor
                    )
                }
            }

            // Limited apps section
            if (uiState.limitedApps.isNotEmpty()) {
                item {
                    if (uiState.activeCategory != AppCategory.LIMITED) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(
                            title = "Limited Access Apps",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(
                    items = uiState.limitedApps,
                    key = { it.appInfo.appName + it.appInfo.packageName }
                ) { appWithCooldown ->
                    AppItemWithCooldown(
                        appWithCooldown = appWithCooldown,
                        onClick = {
                            if (!appWithCooldown.isInCooldown) {
                                context.onTriggerApp(appWithCooldown.appInfo, viewModel::launchApp)
                            }
                            // In the real implementation, you would show a snackbar if in cooldown
                        }
                    )
                }
            }

            // Empty state if no apps match the filter
            if (!uiState.hasResults) {
                item {
                    EmptyState(searchQuery = uiState.searchQuery)
                }
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Search bar component for filtering apps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
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
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "Clear Text",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(30.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 12.dp)
    )
}

/**
 * Category tabs for filtering between all, essential, and limited apps
 */
@Composable
private fun CategoryTabs(
    selectedCategory: AppCategory,
    onCategorySelected: (AppCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryTab(
            title = "All",
            isSelected = selectedCategory == AppCategory.ALL,
            onClick = { onCategorySelected(AppCategory.ALL) },
            modifier = Modifier.weight(1f)
        )

        CategoryTab(
            title = "Essential",
            isSelected = selectedCategory == AppCategory.ESSENTIAL,
            onClick = { onCategorySelected(AppCategory.ESSENTIAL) },
            modifier = Modifier.weight(1f)
        )

        CategoryTab(
            title = "Limited",
            isSelected = selectedCategory == AppCategory.LIMITED,
            onClick = { onCategorySelected(AppCategory.LIMITED) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual category tab
 */
@Composable
private fun CategoryTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animateContainerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant
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

/**
 * Section header for app categories
 */
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

/**
 * Card displaying app info with optional cooldown state
 */
@Composable
private fun AppItemCard(
    name: String,
    icon: Drawable?,
    onClick: () -> Unit,
    isInCooldown: Boolean = false,
    accentColor: Color = Color.Gray
) {
    val cardColor = when {
        isInCooldown -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isInCooldown -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val gradientBrush = Brush.horizontalGradient(
        listOf(
            if (!isInCooldown) accentColor.copy(alpha = 0.1f) else cardColor,
            cardColor,
            cardColor
        ),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .noRippleClickable(enabled = !isInCooldown) { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = if (isInCooldown) {
                        ColorFilter.colorMatrix(
                            ColorMatrix().apply { setToSaturation(0f) }
                        )
                    } else null
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                color = textColor,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Card displaying app info with cooldown state and circular progress
 */
@Composable
private fun AppItemWithCooldown(
    appWithCooldown: AppWithCooldown,
    onClick: () -> Unit
) {
    val app = appWithCooldown.appInfo
    val isInCooldown = appWithCooldown.isInCooldown

    val cardColor = when {
        isInCooldown -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isInCooldown -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val gradientBrush = Brush.horizontalGradient(
        listOf(
            if (!isInCooldown) app.dominantColor.copy(alpha = 0.1f) else cardColor,
            cardColor,
            cardColor
        ),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .noRippleClickable() { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = if (isInCooldown) {
                        ColorFilter.colorMatrix(
                            ColorMatrix().apply { setToSaturation(0f) }
                        )
                    } else null
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // App name
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = app.appName,
                    color = textColor,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis
                )
                if (isInCooldown) {
                    Text(
                        text = "Locked",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Cooldown indicator
            if (isInCooldown) {
                CooldownIndicator(
                    timeRemaining = appWithCooldown.cooldownTimeRemaining,
                    progress = appWithCooldown.cooldownProgress
                )
            }
        }
    }
}

/**
 * Cooldown indicator with circular progress and time remaining
 */
@Composable
private fun CooldownIndicator(
    timeRemaining: Long,
    progress: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp)
    ) {
        // Track (background)
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            strokeWidth = 2.dp
        )

        // Actual progress
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp,
            strokeCap = StrokeCap.Round
        )

        // Time remaining
        Text(
            text = timeRemaining.formatTime(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Empty state when no apps match the search query
 */
@Composable
private fun EmptyState(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No apps found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "No apps match \"$searchQuery\"",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}