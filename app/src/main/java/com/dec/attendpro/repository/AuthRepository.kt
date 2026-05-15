package com.dec.attendpro.repository

import android.util.Log
import com.dec.attendpro.AttendProApp
import com.dec.attendpro.models.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val client = AttendProApp.supabase

    suspend fun signIn(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, profile: UserProfile, idImageBytes: ByteArray?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Sign up the user
            val response = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Get User ID from response (for unconfirmed users) or current session
            val userId = response?.id ?: client.auth.currentUserOrNull()?.id
            
            if (userId == null) {
                return@withContext Result.failure(Exception("Signup initiated. Please check your email to confirm your account before logging in."))
            }
            
            // 2. Upload ID Image
            var imageUrl: String? = null
            if (idImageBytes != null) {
                try {
                    val bucket = client.storage.from("student-ids")
                    val imagePath = "$userId/id_image.jpg"
                    bucket.upload(path = imagePath, data = idImageBytes) { upsert = true }
                    imageUrl = bucket.publicUrl(imagePath)
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Image upload failed: ${e.message}")
                }
            }
            
            // 3. Insert into profiles table
            val profileWithId = profile.copy(id = userId, idImage = imageUrl)
            Log.d("AuthRepository", "Attempting to insert profile for UID: $userId")
            
            try {
                client.postgrest.from("profiles").insert(profileWithId)
                Log.d("AuthRepository", "Profile record created successfully")
            } catch (e: Exception) {
                Log.e("AuthRepository", "Profile DB Insert failed. This is likely an RLS policy issue.", e)
                throw Exception("Auth succeeded but Profile creation failed. Ensure RLS policies allow 'INSERT'. Error: ${e.message}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign up error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthRepository", "Fetching profile for UID: $userId")
            val response = client.postgrest.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
            
            val profiles = response.decodeList<UserProfile>()
            
            if (profiles.isNotEmpty()) {
                Result.success(profiles.first())
            } else {
                Log.e("AuthRepository", "Record not found for $userId. Check RLS Policies in Supabase.")
                Result.failure(Exception("No profile record found. If you just signed up, ensure RLS policies allow reading (SELECT) the 'profiles' table."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Get profile exception: ${e.message}")
            Result.failure(e)
        }
    }

    fun getCurrentUser() = client.auth.currentUserOrNull()
}
