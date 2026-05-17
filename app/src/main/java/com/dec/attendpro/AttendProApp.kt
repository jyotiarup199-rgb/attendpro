package com.dec.attendpro

import android.app.Application
import com.dec.attendpro.utils.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import java.io.File

class AttendProApp : Application() {

    companion object {
        lateinit var supabase: SupabaseClient
    }

    override fun onCreate() {
        super.onCreate()

        // Create the student_faces folder in internal storage
        val directory = File(filesDir, "student_faces")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        supabase = createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
