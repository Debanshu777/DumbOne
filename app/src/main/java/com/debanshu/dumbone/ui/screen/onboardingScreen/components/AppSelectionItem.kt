package com.debanshu.dumbone.ui.screen.onboardingScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(
                        alpha = 0.6f
                    )
                )
            }
        }
    }
}