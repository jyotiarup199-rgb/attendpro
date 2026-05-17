package com.dec.attendpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dec.attendpro.data.local.dao.AttendanceDao
import com.dec.attendpro.data.local.dao.StudentDao
import com.dec.attendpro.data.local.entity.AttendanceEntity
import com.dec.attendpro.data.local.entity.StudentEntity

@Database(entities = [StudentEntity::class, AttendanceEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendpro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
