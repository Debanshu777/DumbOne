package com.debanshu.dumbone.ui.screen.homeScreen


import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.debanshu.dumbone.ui.screen.homeScreen.components.ClockDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val essentialApps by viewModel.essentialApps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ClockDisplay()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL)
                    context.startActivity(intent)

                    // Find the phone app package name and record usage
                    essentialApps.find {
                        it.packageName.contains("dialer") ||
                                it.packageName.contains("phone")
                    }?.let {
                        viewModel.launchApp(it.packageName)
                    }
                }
            ) {
                Icon(
                    Icons.Outlined.Call,
                    contentDescription = "Call",
                    tint =  MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
                    context.startActivity(intent)

                    // Find the messaging app package name and record usage
                    essentialApps.find {
                        it.packageName.contains("messag") ||
                                it.packageName.contains("mms") ||
                                it.packageName.contains("sms")
                    }?.let {
                        viewModel.launchApp(it.packageName)
                    }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Message,
                    contentDescription = "Call",
                    tint =  MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
