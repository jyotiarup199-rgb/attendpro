package com.dec.attendpro.repository

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
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, profile: UserProfile, idImageBytes: ByteArray?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Sign up the user
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = client.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found after signup")
            
            // 2. Upload ID Image if provided
            var imageUrl: String? = null
            if (idImageBytes != null) {
                val bucket = client.storage.from("student-ids")
                val imagePath = "$userId/id_image.jpg"
                
                // Using explicit data argument to help compiler resolution
                bucket.upload(path = imagePath, data = idImageBytes) {
                    upsert = true
                }
                imageUrl = bucket.publicUrl(imagePath)
            }
            
            // 3. Insert profile into public.profiles table
            val profileWithId = profile.copy(id = userId, idImage = imageUrl)
            client.postgrest.from("profiles").insert(profileWithId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val profile = client.postgrest.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        // client.auth.signOut() is suspend, should be called in a scope
    }

    fun getCurrentUser() = client.auth.currentUserOrNull()
}
