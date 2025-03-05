package com.debanshu.dumbone.ui.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MinimalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (enabled) colors.primary else colors.secondary.copy(alpha = 0.5f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) colors.onPrimary else colors.onSecondary.copy(alpha = 0.7f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MinimalIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier.alpha(if (enabled) 1f else 0.5f),
            tint =  MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun AppItem(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isInCooldown: Boolean = false,
    cooldownTimeRemaining: Long = 0
) {
    val colors =  MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isInCooldown) { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            color = if (isInCooldown) colors.secondary else colors.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (isInCooldown && cooldownTimeRemaining > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Available in: ${formatTime(cooldownTimeRemaining)}",
                fontSize = 12.sp,
                color = colors.secondary
            )
        }
    }
}

@Composable
fun ClockDisplay(
    currentTime: Long,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeString = dateFormat.format(Date(currentTime))

    val dateFormatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val dateString = dateFormatter.format(Date(currentTime))

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeString,
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            color =  MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = dateString,
            fontSize = 16.sp,
            color =  MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color =  MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Divider(
            color =  MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun UsageStatItem(
    appName: String,
    usageCount: Int,
    usageDuration: Long,
    modifier: Modifier = Modifier
) {
    val colors =  MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = appName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Opened $usageCount times",
                fontSize = 14.sp,
                color = colors.secondary
            )

            Text(
                text = "Total: ${formatTime(usageDuration)}",
                fontSize = 14.sp,
                color = colors.secondary
            )
        }
    }
}

@Composable
fun CooldownTimer(
    timeRemaining: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Time until available:",
            fontSize = 14.sp,
            color =  MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatTime(timeRemaining),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color =  MaterialTheme.colorScheme.onBackground
        )
    }
}

// Helper function to format time
fun formatTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
}