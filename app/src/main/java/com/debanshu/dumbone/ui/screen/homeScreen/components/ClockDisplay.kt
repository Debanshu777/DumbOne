package com.debanshu.dumbone.ui.screen.homeScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockDisplay(
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            val currentTimeMillis = System.currentTimeMillis()
            val delayToNextMinute = 60000 - (currentTimeMillis % 60000)
            delay(delayToNextMinute)
        }
    }

    val timeString by remember(currentTime) {
        derivedStateOf {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(currentTime))
        }
    }

    val dateString by remember(currentTime) {
        derivedStateOf {
            SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(currentTime))
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        Text(
            text = timeString,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
            color = Color.White
        )

        Text(
            text = dateString,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}