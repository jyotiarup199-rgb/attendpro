package com.dec.attendpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dec.attendpro.data.local.AppDatabase
import com.dec.attendpro.data.local.entity.AttendanceEntity
import com.dec.attendpro.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow

class AttendanceHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AttendanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).attendanceDao()
        repository = AttendanceRepository(dao)
    }

    val allAttendance: Flow<List<AttendanceEntity>> = repository.allAttendance
}
