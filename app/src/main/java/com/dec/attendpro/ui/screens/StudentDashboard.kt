package com.dec.attendpro.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dec.attendpro.ui.components.AttendProCard
import com.dec.attendpro.ui.components.BottomNavigationBar
import com.dec.attendpro.ui.models.AttendanceRecord
import com.dec.attendpro.ui.models.mockAttendanceHistory
import com.dec.attendpro.ui.theme.ErrorRed
import com.dec.attendpro.ui.theme.SuccessGreen

@Composable
fun StudentDashboard(onNavigateToProfile: () -> Unit) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "home",
                onItemSelected = { screen ->
                    if (screen == "profile") onNavigateToProfile()
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                StudentHeader()
                Spacer(modifier = Modifier.height(32.dp))
                AttendanceOverview()
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Recent History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(mockAttendanceHistory) { history ->
                HistoryItem(history)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StudentHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back,",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Alex Johnson",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AttendanceOverview() {
    AttendProCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Overall Attendance",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "88.5%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Good Standing",
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            CircularProgress(percentage = 0.885f)
        }
    }
}

@Composable
fun CircularProgress(percentage: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = primaryColor.copy(alpha = 0.1f)
    
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(
                color = trackColor,
                style = Stroke(width = 8.dp.toPx())
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360 * percentage,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(percentage * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HistoryItem(record: AttendanceRecord) {
    AttendProCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (record.isPresent) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (record.isPresent) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (record.isPresent) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = record.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (record.isPresent) "Present" else "Absent",
                color = if (record.isPresent) SuccessGreen else ErrorRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
