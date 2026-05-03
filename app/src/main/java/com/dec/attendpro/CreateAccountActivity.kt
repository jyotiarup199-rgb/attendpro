package com.dec.attendpro

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dec.attendpro.models.UserProfile
import com.dec.attendpro.repository.AuthRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreateAccountActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private var selectedImageUri: Uri? = null
    private lateinit var ivIdImage: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivIdImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val llStudentFields = findViewById<LinearLayout>(R.id.llStudentFields)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etRollNumber = findViewById<EditText>(R.id.etRollNumber)
        val etSemester = findViewById<EditText>(R.id.etSemester)
        val etBranch = findViewById<EditText>(R.id.etBranch)
        ivIdImage = findViewById(R.id.ivIdImage)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        val cardIdImage = findViewById<View>(R.id.cardIdImage)

        rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbStudent) {
                llStudentFields.visibility = View.VISIBLE
            } else {
                llStudentFields.visibility = View.GONE
            }
        }

        cardIdImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnCreateAccount.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val role = if (findViewById<RadioButton>(R.id.rbStudent).isChecked) "Student" else "Teacher"

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all basic details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roll = etRollNumber.text.toString().trim()
            val sem = etSemester.text.toString().trim()
            val branch = etBranch.text.toString().trim()

            if (role == "Student" && (roll.isEmpty() || sem.isEmpty() || branch.isEmpty())) {
                Toast.makeText(this, "Please fill student details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                btnCreateAccount.isEnabled = false
                val idImageBytes = selectedImageUri?.let { uriToBytes(it) }
                
                val profile = UserProfile(
                    id = "", // Will be set in repository
                    name = name,
                    email = email,
                    role = role,
                    rollNumber = if (role == "Student") roll else null,
                    semester = if (role == "Student") sem else null,
                    branch = if (role == "Student") branch else null,
                    phoneNumber = phone
                )

                val result = authRepository.signUp(email, password, profile, idImageBytes)
                
                if (result.isSuccess) {
                    Toast.makeText(this@CreateAccountActivity, "Account created! Please login.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateAccountActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    btnCreateAccount.isEnabled = true
                }
            }
        }
    }

    private fun uriToBytes(uri: Uri): ByteArray? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
