package com.debanshu.dumbone.ui.common


import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.debanshu.dumbone.data.model.AppInfo
import java.util.concurrent.TimeUnit

fun Long.formatDuration(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60

    return when {
        hours > 0 -> "$hours h $minutes min"
        else -> "$minutes min"
    }
}


fun Context.onTriggerApp(
    app: AppInfo,
    onAppLaunch: (String) -> Unit
) {
    try {
        val launchIntent = this.packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            onAppLaunch(app.packageName)
            this.startActivity(launchIntent)
        }
    } catch (e: Exception) {
        // Handle exception
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}