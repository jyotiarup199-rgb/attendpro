package com.dec.attendpro.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dec.attendpro.camera.CameraManager
import com.dec.attendpro.viewmodel.EnrollmentUiState
import com.dec.attendpro.viewmodel.EnrollmentViewModel

@Composable
fun RegisterStudentScreen(
    onBack: () -> Unit,
    viewModel: EnrollmentViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentFaceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val cameraManager = remember { CameraManager(context) }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is EnrollmentUiState.Success -> {
                Toast.makeText(context, (uiState as EnrollmentUiState.Success).message, Toast.LENGTH_SHORT).show()
                onBack()
            }
            is EnrollmentUiState.Error -> {
                Toast.makeText(context, (uiState as EnrollmentUiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register New Student", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))

        if (capturedBitmap == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            cameraManager.startCamera(
                                previewView = this,
                                lifecycleOwner = lifecycleOwner,
                                useFrontCamera = true,
                                onFacesDetected = { _, _, _ -> },
                                onFaceCropped = { bitmap ->
                                    currentFaceBitmap = bitmap
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (currentFaceBitmap != null) {
                        capturedBitmap = currentFaceBitmap
                    } else {
                        Toast.makeText(context, "No face detected yet. Please look at the camera.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Capture Face")
            }
        } else {
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "Captured Face",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            TextButton(onClick = { capturedBitmap = null }) {
                Text("Retake Photo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("Student ID / Roll Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is EnrollmentUiState.Processing) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    capturedBitmap?.let {
                        viewModel.registerStudent(name, studentId, it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && studentId.isNotBlank() && capturedBitmap != null
            ) {
                Text("Register Student")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.shutdown()
        }
    }
}
