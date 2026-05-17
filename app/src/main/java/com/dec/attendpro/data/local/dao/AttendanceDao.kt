package com.dec.attendpro.data.local.dao

import androidx.room.*
import com.dec.attendpro.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND timestamp > :since")
    suspend fun getRecentAttendance(studentId: String, since: Long): List<AttendanceEntity>
}
