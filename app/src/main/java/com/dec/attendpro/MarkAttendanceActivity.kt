package com.dec.attendpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dec.attendpro.ui.screens.AttendanceCameraScreen
import com.dec.attendpro.ui.theme.AttendProTheme

class MarkAttendanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AttendProTheme {
                AttendanceCameraScreen(
                    onClose = { finish() }
                )
            }
        }
    }
}
