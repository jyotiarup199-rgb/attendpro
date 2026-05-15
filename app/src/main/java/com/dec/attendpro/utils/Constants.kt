package com.dec.attendpro.utils

object Constants {
    const val SUPABASE_URL = "https://wxoioveweundgleczunp.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind4b2lvdmV3ZXVuZGdsZWN6dW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc1NjM5NDEsImV4cCI6MjA5MzEzOTk0MX0.N2X11xshAkCggq_-xGu3drk96WgaDS-_5FLsnb992jM"

    // Face Recognition API Base URL
    // For emulator: use 10.0.2.2 (maps to host machine's localhost)
    // For physical device: use your machine's local IP (e.g., 192.168.x.x)
    // For production: use your deployed URL (e.g., https://myapp.railway.app)
    const val FACE_API_BASE_URL = "http://0.0.0.0:5000/"
}
