package com.dec.attendpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dec.attendpro.ui.components.AttendProButton
import com.dec.attendpro.ui.components.AttendProCard
import com.dec.attendpro.ui.components.BottomNavigationBar
import com.dec.attendpro.ui.models.ClassInfo
import com.dec.attendpro.ui.models.mockTodayClasses
import com.dec.attendpro.ui.theme.GradientEnd
import com.dec.attendpro.ui.theme.GradientStart

@Composable
fun TeacherDashboard(
    onStartAttendance: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "home",
                onItemSelected = { screen ->
                    when (screen) {
                        "analytics" -> onNavigateToAnalytics()
                        "camera" -> onStartAttendance()
                        "profile" -> onNavigateToProfile()
                        "home" -> {}
                    }
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
                HeaderSection()
                Spacer(modifier = Modifier.height(32.dp))
                StatsSection()
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Classes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToStudents) {
                        Text("Manage Students")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(mockTodayClasses) { classInfo ->
                ClassCard(classInfo)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                AttendProButton(
                    text = "Start AI Attendance",
                    onClick = onStartAttendance
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning,",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dr. Robert Fox",
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
fun StatsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Total Students",
            value = "124",
            icon = Icons.Default.Groups,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Avg. Present",
            value = "92%",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    AttendProCard(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ClassCard(classInfo: ClassInfo) {
    AttendProCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.verticalGradient(listOf(GradientStart, GradientEnd)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = classInfo.subject.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = classInfo.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "${classInfo.time} • ${classInfo.room}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
