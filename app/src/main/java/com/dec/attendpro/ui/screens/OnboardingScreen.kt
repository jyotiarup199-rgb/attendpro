package com.dec.attendpro.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dec.attendpro.ui.components.AttendProButton
import kotlinx.coroutines.launch

data class OnboardingItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val onboardingItems = listOf(
    OnboardingItem(
        "Face Recognition",
        "Mark attendance effortlessly using AI-powered face recognition technology.",
        Icons.Default.Face
    ),
    OnboardingItem(
        "Real-time Analytics",
        "Get instant insights into student attendance trends and performance.",
        Icons.Default.Analytics
    ),
    OnboardingItem(
        "Smart Alerts",
        "Receive automated notifications for low attendance and important updates.",
        Icons.Default.NotificationsActive
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = onboardingItems[page]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = item.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.description,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.height(50.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(onboardingItems.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .background(color, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AttendProButton(
            text = if (pagerState.currentPage == onboardingItems.size - 1) "Get Started" else "Next",
            onClick = {
                if (pagerState.currentPage < onboardingItems.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinish()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (pagerState.currentPage < onboardingItems.size - 1) {
            TextButton(onClick = onFinish) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
