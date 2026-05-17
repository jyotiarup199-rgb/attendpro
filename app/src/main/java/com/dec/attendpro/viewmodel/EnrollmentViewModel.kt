package com.dec.attendpro.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dec.attendpro.data.local.AppDatabase
import com.dec.attendpro.data.local.entity.StudentEntity
import com.dec.attendpro.repository.StudentRepository
import com.dec.attendpro.utils.FaceNetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class EnrollmentUiState {
    object Idle : EnrollmentUiState()
    object Processing : EnrollmentUiState()
    data class Success(val message: String) : EnrollmentUiState()
    data class Error(val message: String) : EnrollmentUiState()
}

class EnrollmentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudentRepository
    private val faceNetHelper = FaceNetHelper(application)
    
    private val _uiState = MutableStateFlow<EnrollmentUiState>(EnrollmentUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        val studentDao = AppDatabase.getDatabase(application).studentDao()
        repository = StudentRepository(studentDao)
    }

    fun registerStudent(name: String, studentId: String, faceBitmap: Bitmap) {
        if (name.isBlank() || studentId.isBlank()) {
            _uiState.value = EnrollmentUiState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = EnrollmentUiState.Processing
            try {
                // Check if student already exists
                val existing = withContext(Dispatchers.IO) {
                    repository.getStudentById(studentId)
                }
                
                if (existing != null) {
                    _uiState.value = EnrollmentUiState.Error("Student ID already registered")
                    return@launch
                }

                // Generate embedding on background thread
                val embedding = withContext(Dispatchers.Default) {
                    faceNetHelper.getEmbedding(faceBitmap)
                }
                
                // Save to Room on IO thread
                withContext(Dispatchers.IO) {
                    val student = StudentEntity(
                        studentId = studentId,
                        name = name,
                        faceEmbedding = embedding
                    )
                    repository.insertStudent(student)
                }
                
                Log.d("EnrollmentVM", "Student $name registered with ID $studentId")
                _uiState.value = EnrollmentUiState.Success("Student registered successfully!")
            } catch (e: Exception) {
                Log.e("EnrollmentVM", "Registration failed", e)
                _uiState.value = EnrollmentUiState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = EnrollmentUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        faceNetHelper.close()
    }
}
