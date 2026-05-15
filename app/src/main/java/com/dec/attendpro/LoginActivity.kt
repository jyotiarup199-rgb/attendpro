package com.dec.attendpro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dec.attendpro.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)

        // Hide role selection buttons on login screen as we'll fetch role from database
        findViewById<View>(R.id.btnTeacherRole).visibility = View.GONE
        findViewById<View>(R.id.btnStudentRole).visibility = View.GONE
        tvSubtitle.text = "Sign in to access your dashboard"

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            val result = authRepository.signIn(email, password)
            if (result.isSuccess) {
                val userId = authRepository.getCurrentUser()?.id
                if (userId != null) {
                    val profileResult = authRepository.getUserProfile(userId)
                    if (profileResult.isSuccess) {
                        val profile = profileResult.getOrNull()
                        if (profile != null) {
                            Toast.makeText(this@LoginActivity, "Welcome ${profile.name}", Toast.LENGTH_SHORT).show()
                            navigateToMain(profile.role)
                        } else {
                            Toast.makeText(this@LoginActivity, "Profile not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val error = profileResult.exceptionOrNull()?.message ?: "Unknown error"
                        Log.e("LoginActivity", "Profile load failed: $error")
                        Toast.makeText(this@LoginActivity, "Failed to load profile: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                Toast.makeText(this@LoginActivity, "Login failed: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToMain(role: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_ROLE", role)
        startActivity(intent)
        finish()
    }
}
