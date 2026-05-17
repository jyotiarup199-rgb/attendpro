package com.dec.attendpro.repository

import android.content.Context
import android.util.Log
import com.dec.attendpro.AttendProApp
import com.dec.attendpro.data.local.AppDatabase
import com.dec.attendpro.data.local.entity.StudentEntity
import com.dec.attendpro.models.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AuthRepository {

    private val client = AttendProApp.supabase

    suspend fun signIn(
        email: String,
        password: String
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {

            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            Result.success(Unit)

        } catch (e: Exception) {

            Log.e(
                "AuthRepository",
                "Sign in error: ${e.message}"
            )

            Result.failure(e)
        }
    }

    suspend fun signUp(
        context: Context,
        email: String,
        password: String,
        profile: UserProfile,
        idImageBytes: ByteArray?,
        centerFace: ByteArray?,
        rightFace: ByteArray?,
        leftFace: ByteArray?
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {

            // SIGNUP USER
            val response =
                client.auth.signUpWith(Email) {

                    this.email = email
                    this.password = password
                }

            val userId =
                response?.id
                    ?: client.auth.currentUserOrNull()?.id

            if (userId == null) {

                return@withContext Result.failure(
                    Exception(
                        "Signup failed. User ID is null."
                    )
                )
            }

            // SAVE FACE IMAGES LOCALLY
            val directory =
                File(context.filesDir, "student_faces")

            if (!directory.exists()) {
                directory.mkdirs()
            }

            fun saveFace(
                data: ByteArray?,
                suffix: String
            ): String? {

                if (data == null) return null

                return try {

                    val fileName =
                        if (suffix.isEmpty()) {
                            "$userId.jpg"
                        } else {
                            "${userId}_$suffix.jpg"
                        }

                    val imageFile =
                        File(directory, fileName)

                    FileOutputStream(imageFile).use {
                        it.write(data)
                    }

                    imageFile.absolutePath

                } catch (e: Exception) {

                    Log.e(
                        "AuthRepository",
                        "Error saving face $suffix",
                        e
                    )

                    null
                }
            }

            val centerPath =
                saveFace(centerFace, "")

            saveFace(rightFace, "right")

            saveFace(leftFace, "left")

            val idCardPath =
                saveFace(idImageBytes, "id_card")

            // PROFILE WITH ID
            val profileWithId =
                profile.copy(
                    id = userId,
                    idImage = idCardPath ?: centerPath
                )

            Log.d(
                "AuthRepository",
                "Embedding before insert: ${
                    profileWithId.faceEmbedding
                }"
            )

            Log.d(
                "AuthRepository",
                "Attempting profile insert for UID: $userId"
            )

            // INSERT INTO SUPABASE
            try {

                client.postgrest
                    .from("profiles")
                    .insert(profileWithId)

                Log.d(
                    "AuthRepository",
                    "Profile inserted into Supabase"
                )

            } catch (e: Exception) {

                Log.e(
                    "AuthRepository",
                    "Supabase insert failed",
                    e
                )

                throw Exception(
                    "Supabase insert failed: ${e.message}"
                )
            }

            // SAVE TO ROOM DATABASE
            try {

                val database =
                    AppDatabase.getDatabase(context)

                val studentDao =
                    database.studentDao()

                val embedding =
                    profileWithId.faceEmbedding
                        ?: emptyList()

                val studentEntity =
                    StudentEntity(
                        studentId = userId,
                        name = profileWithId.name,
                        faceEmbedding =
                            embedding.toFloatArray()
                    )

                studentDao.insertStudent(studentEntity)

                Log.d(
                    "AuthRepository",
                    "Student saved to Room DB"
                )

            } catch (e: Exception) {

                Log.e(
                    "AuthRepository",
                    "Room DB insert failed",
                    e
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {

            Log.e(
                "AuthRepository",
                "Sign up error",
                e
            )

            Result.failure(e)
        }
    }

    suspend fun getUserProfile(
        userId: String
    ): Result<UserProfile> =
        withContext(Dispatchers.IO) {

            try {

                Log.d(
                    "AuthRepository",
                    "Fetching profile for UID: $userId"
                )

                val response =
                    client.postgrest
                        .from("profiles")
                        .select {
                            filter {
                                eq("id", userId)
                            }
                        }

                val profiles =
                    response.decodeList<UserProfile>()

                if (profiles.isNotEmpty()) {

                    Result.success(
                        profiles.first()
                    )

                } else {

                    Result.failure(
                        Exception(
                            "Profile not found"
                        )
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "AuthRepository",
                    "Get profile exception",
                    e
                )

                Result.failure(e)
            }
        }

    fun getCurrentUser() =
        client.auth.currentUserOrNull()
}