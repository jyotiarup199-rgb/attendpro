package com.dec.attendpro.repository

import com.dec.attendpro.data.local.dao.StudentDao
import com.dec.attendpro.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()

    suspend fun insertStudent(student: StudentEntity) {
        studentDao.insertStudent(student)
    }

    suspend fun getStudentById(id: String): StudentEntity? {
        return studentDao.getStudentById(id)
    }

    suspend fun getAllStudentsList(): List<StudentEntity> {
        return studentDao.getAllStudentsList()
    }

    suspend fun getAllStudentsOnce(): List<StudentEntity> {
        return studentDao.getAllStudentsOnce()
    }
}
