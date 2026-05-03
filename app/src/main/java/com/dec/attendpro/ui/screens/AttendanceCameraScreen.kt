package com.dec.attendpro.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dec.attendpro.ui.models.StudentBrief
import com.dec.attendpro.ui.models.mockStudentList
import com.dec.attendpro.ui.theme.SuccessGreen

@Composable
fun AttendanceCameraScreen(onClose: () -> Unit) {
    var isAutoScan by remember { mutableStateOf(true) }
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanAlpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Mock Camera Viewport
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp)
                .background(Color.DarkGray)
        ) {
            // Scanning Line
            if (isAutoScan) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.Center)
                        .background(SuccessGreen.copy(alpha = scanAlpha))
                )
            }

            // Face Detection Overlay (Mock)
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.Center)
                    .border(2.dp, SuccessGreen, RoundedCornerShape(12.dp))
            ) {
                Surface(
                    color = SuccessGreen,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = "98% MATCH - ROBERT FOX",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AUTO SCAN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isAutoScan,
                    onCheckedChange = { isAutoScan = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SuccessGreen,
                        checkedTrackColor = SuccessGreen.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // Bottom UI
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(bottom = 24.dp)
        ) {
            // Handle for sliding up
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recognized (14)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* Manual Override */ }) {
                    Text("Manual Entry")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockStudentList) { student ->
                    RecognizedStudentItem(student)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Flip */ },
                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip")
                }
                
                // Capture Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                        .padding(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Camera, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                IconButton(
                    onClick = { onClose() },
                    modifier = Modifier.size(48.dp).background(SuccessGreen.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Done", tint = SuccessGreen)
                }
            }
        }
    }
}

@Composable
fun RecognizedStudentItem(student: StudentBrief) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(student.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            // Small check badge
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(student.name.split(" ")[0], fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
