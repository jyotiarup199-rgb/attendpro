package com.dec.attendpro.data.local.dao

import androidx.room.*
import com.dec.attendpro.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsList(): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsOnce(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE studentId = :id")
    suspend fun getStudentById(id: String): StudentEntity?
}
