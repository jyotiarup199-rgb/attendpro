package com.dec.attendpro.repository

import com.dec.attendpro.data.local.dao.AttendanceDao
import com.dec.attendpro.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()

    suspend fun insertAttendance(attendance: AttendanceEntity) {
        attendanceDao.insertAttendance(attendance)
    }

    suspend fun isAlreadyMarked(studentId: String): Boolean {
        // Simple logic: already marked if there's an entry in the last 10 minutes
        val tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000)
        return attendanceDao.getRecentAttendance(studentId, tenMinutesAgo).isNotEmpty()
    }
}
