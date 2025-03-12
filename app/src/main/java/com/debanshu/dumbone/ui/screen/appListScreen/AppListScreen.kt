package com.debanshu.dumbone.ui.screen.appListScreen


import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val essentialApps by viewModel.essentialApps.collectAsState()
    val limitedAppsWithCooldowns by viewModel.limitedAppsWithCooldowns.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("all") } // "all", "essential", "limited"

    // Filter apps based on search query and category
    val filteredEssentialApps = essentialApps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) &&
                (activeCategory == "all" || activeCategory == "essential")
    }

    val filteredLimitedApps = limitedAppsWithCooldowns.filter {
        it.appInfo.appName.contains(searchQuery, ignoreCase = true) &&
                (activeCategory == "all" || activeCategory == "limited")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
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
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 12.dp),
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
        // Essential apps section
        if (filteredEssentialApps.isNotEmpty()) {
            item {
                if (activeCategory != "essential") {
                    Text(
                        text = "Essential Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(filteredEssentialApps) { app ->
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
        if (filteredLimitedApps.isNotEmpty()) {
            item {
                if (activeCategory != "limited") {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Limited Access Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(filteredLimitedApps) { appWithCooldown ->
                AppItemCard(
                    name = appWithCooldown.appInfo.appName,
                    icon = appWithCooldown.appInfo.icon,
                    onClick = {
                        context.onTriggerApp(appWithCooldown.appInfo, viewModel::launchApp)
                    },
                    isInCooldown = appWithCooldown.isInCooldown,
                    cooldownTimeRemaining = appWithCooldown.cooldownTimeRemaining,
                    accentColor = appWithCooldown.appInfo.dominantColor
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

@Composable
fun AppItemCard(
    name: String,
    icon: Drawable?,
    onClick: () -> Unit,
    isInCooldown: Boolean = false,
    cooldownTimeRemaining: Long = 0,
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

    val gradientRadial = Brush.horizontalGradient(
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
            .clickable(enabled = !isInCooldown) { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientRadial)
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

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                        color = textColor,
                        text = "Available in: ${cooldownTimeRemaining.formatTime()}",
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}