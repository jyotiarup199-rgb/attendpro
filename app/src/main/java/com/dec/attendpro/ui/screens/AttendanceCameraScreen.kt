package com.dec.attendpro.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dec.attendpro.camera.CameraManager
import com.dec.attendpro.ui.theme.ErrorRed
import com.dec.attendpro.ui.theme.SuccessGreen
import com.dec.attendpro.viewmodel.FaceRecognitionViewModel
import com.dec.attendpro.viewmodel.RecognitionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class RecognizedPerson(
    val name: String,
    val studentId: String,
    val confidence: Float
)

@Composable
fun AttendanceCameraScreen(
    onClose: () -> Unit,
    viewModel: FaceRecognitionViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isAutoScan by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(true) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraManager = remember { CameraManager(context) }
    val recognitionState by viewModel.recognitionState.collectAsState()
    val recognizedList = remember { mutableStateListOf<RecognizedPerson>() }

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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(isAutoScan) {
        viewModel.setScanning(isAutoScan)
    }

    LaunchedEffect(recognitionState) {
        if (recognitionState is RecognitionState.Recognized) {
            val state = recognitionState as RecognitionState.Recognized
            if (state.confidence >= 0.80f && recognizedList.none { it.studentId == state.studentId }) {
                recognizedList.add(RecognizedPerson(state.name, state.studentId, state.confidence))
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraManager.shutdown() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 180.dp)) {
            if (hasCameraPermission) {
                key(useFrontCamera) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                cameraManager.startCamera(
                                    previewView = previewView,
                                    lifecycleOwner = lifecycleOwner,
                                    useFrontCamera = useFrontCamera,
                                    onFaceDetected = { bitmap, embedding ->
                                        // Fixed the null!! crash
                                        viewModel.onFaceDetected(bitmap, embedding)
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (isAutoScan) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.Center)
                            .background(SuccessGreen.copy(alpha = scanAlpha))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .align(Alignment.Center)
                        .border(
                            2.dp,
                            when (recognitionState) {
                                is RecognitionState.Recognized -> SuccessGreen
                                is RecognitionState.Unknown -> ErrorRed
                                is RecognitionState.Scanning -> Color.Yellow
                                else -> Color.White.copy(alpha = 0.5f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    val labelText = when (recognitionState) {
                        is RecognitionState.Recognized -> {
                            val s = recognitionState as RecognitionState.Recognized
                            "${(s.confidence * 100).toInt()}% MATCH - ${s.name.uppercase()}"
                        }
                        is RecognitionState.Unknown -> "UNKNOWN FACE"
                        is RecognitionState.Scanning -> "SCANNING..."
                        else -> ""
                    }
                    if (labelText.isNotEmpty()) {
                        Surface(
                            color = when (recognitionState) {
                                is RecognitionState.Recognized -> SuccessGreen
                                is RecognitionState.Unknown -> ErrorRed
                                else -> Color.Yellow
                            },
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Text(
                                text = labelText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Top Controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Row(
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AUTO SCAN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = isAutoScan, onCheckedChange = { isAutoScan = it })
            }
        }

        // Bottom UI
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).padding(bottom = 24.dp)
        ) {
            Box(modifier = Modifier.padding(vertical = 12.dp).width(40.dp).height(4.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape).align(Alignment.CenterHorizontally))
            Text("Recognized (${recognizedList.size})", modifier = Modifier.padding(horizontal = 24.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            if (recognizedList.isEmpty()) {
                Text("No faces recognized. Enable Auto Scan.", modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            } else {
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(recognizedList) { person ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(person.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(person.name.split(" ")[0], fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { useFrontCamera = !useFrontCamera }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip")
                }
                IconButton(onClick = { if (!isAutoScan) { viewModel.setScanning(true); coroutineScope.launch { delay(2000); if (!isAutoScan) viewModel.setScanning(false) } } }, modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape)) {
                    Icon(Icons.Default.Camera, contentDescription = "Capture", tint = Color.White)
                }
                IconButton(onClick = onClose, modifier = Modifier.background(SuccessGreen.copy(alpha = 0.2f), CircleShape)) {
                    Icon(Icons.Default.Check, contentDescription = "Done", tint = SuccessGreen)
                }
            }
        }
    }
}
