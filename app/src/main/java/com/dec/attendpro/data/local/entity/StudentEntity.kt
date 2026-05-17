package com.dec.attendpro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val name: String,
    val faceEmbedding: FloatArray,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as StudentEntity
        if (studentId != other.studentId) return false
        return true
    }

    override fun hashCode(): Int {
        return studentId.hashCode()
    }
}
