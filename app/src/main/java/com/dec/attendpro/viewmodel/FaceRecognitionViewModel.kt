package com.dec.attendpro.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dec.attendpro.data.local.AppDatabase
import com.dec.attendpro.data.local.entity.AttendanceEntity
import com.dec.attendpro.data.local.entity.StudentEntity
import com.dec.attendpro.repository.AttendanceRepository
import com.dec.attendpro.repository.StudentRepository
import com.dec.attendpro.utils.FaceNetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

sealed class RecognitionState {
    object Idle : RecognitionState()
    object Scanning : RecognitionState()
    data class Recognized(val name: String, val studentId: String, val confidence: Float) : RecognitionState()
    object Unknown : RecognitionState()
}

class FaceRecognitionViewModel(application: Application) : AndroidViewModel(application) {
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()

    private val faceNetHelper = FaceNetHelper(application)
    private val studentRepository: StudentRepository
    private val attendanceRepository: AttendanceRepository
    
    private var studentEmbeddings = listOf<StudentEntity>()
    private var isScanning = false
    private var lastRecognitionTime = 0L
    private val processingThreshold = 1000L // Process max 1 face per second to save battery/CPU

    init {
        val database = AppDatabase.getDatabase(application)
        studentRepository = StudentRepository(database.studentDao())
        attendanceRepository = AttendanceRepository(database.attendanceDao())
        loadStudentsFromDb()
    }

    private fun loadStudentsFromDb() {

        viewModelScope.launch {

            try {

                val students =
                    studentRepository.getAllStudentsOnce()

                studentEmbeddings = students

                Log.d(
                    "FaceRecognitionVM",
                    "Loaded ${students.size} students"
                )

                for (student in students) {

                    Log.d(
                        "FaceRecognitionVM",
                        "Student: ${student.name}"
                    )

                    Log.d(
                        "FaceRecognitionVM",
                        "Embedding size: ${student.faceEmbedding.size}"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "FaceRecognitionVM",
                    "Failed loading students",
                    e
                )
            }
        }
    }

    fun setScanning(scanning: Boolean) {
        isScanning = scanning
        _recognitionState.value = if (scanning) RecognitionState.Scanning else RecognitionState.Idle
        Log.d("FaceRecognitionVM", "Scanning set to: $scanning")
    }

    fun onFaceDetected(bitmap: Bitmap) {
        if (!isScanning || studentEmbeddings.isEmpty()) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRecognitionTime < processingThreshold) return

        viewModelScope.launch {
            try {
                // Run embedding generation on Default dispatcher (CPU intensive)
                val currentEmbedding = withContext(Dispatchers.Default) {
                    faceNetHelper.getEmbedding(bitmap)
                }
                
                var bestMatch: StudentEntity? = null
                var maxSimilarity = -1f

                // Compare with all stored embeddings
                for (student in studentEmbeddings) {

                    val similarity =
                        cosineSimilarity(
                            currentEmbedding,
                            student.faceEmbedding
                        )

                    Log.d(
                        "FaceRecognitionVM",
                        "Comparing with ${student.name}"
                    )

                    Log.d(
                        "FaceRecognitionVM",
                        "Similarity: $similarity"
                    )

                    if (similarity > maxSimilarity) {

                        maxSimilarity = similarity
                        bestMatch = student
                    }
                }

                // Threshold for FaceNet (Cosine Similarity: 0.75 - 0.90 is good)
                if (maxSimilarity > 0.45f && bestMatch != null) {
                    val recognized = RecognitionState.Recognized(
                        name = bestMatch.name,
                        studentId = bestMatch.studentId,
                        confidence = maxSimilarity
                    )
                    _recognitionState.value = recognized
                    Log.d("FaceRecognitionVM", "Recognized: ${bestMatch.name} with confidence: $maxSimilarity")
                    
                    // Mark attendance
                    markAttendance(bestMatch, maxSimilarity)
                } else {
                    _recognitionState.value = RecognitionState.Unknown
                    Log.d("FaceRecognitionVM", "Face not recognized (max similarity: $maxSimilarity)")
                }
                
                lastRecognitionTime = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("FaceRecognitionVM", "Error during face recognition", e)
            }
        }
    }

    private fun markAttendance(student: StudentEntity, confidence: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Prevent duplicate attendance within 10 minutes
                if (!attendanceRepository.isAlreadyMarked(student.studentId)) {
                    val attendance = AttendanceEntity(
                        studentId = student.studentId,
                        studentName = student.name,
                        confidence = confidence
                    )
                    attendanceRepository.insertAttendance(attendance)
                    Log.d("FaceRecognitionVM", "Attendance recorded for ${student.name}")
                } else {
                    Log.d("FaceRecognitionVM", "Attendance already marked recently for ${student.name}")
                }
            } catch (e: Exception) {
                Log.e("FaceRecognitionVM", "Failed to record attendance", e)
            }
        }
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        if (vec1.size != vec2.size) return 0f
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            normA += vec1[i] * vec1[i]
            normB += vec2[i] * vec2[i]
        }
        val result = dotProduct / (sqrt(normA.toDouble()) * sqrt(normB.toDouble())).toFloat()
        return if (result.isNaN()) 0f else result
    }

    override fun onCleared() {
        super.onCleared()
        faceNetHelper.close()
    }
}
