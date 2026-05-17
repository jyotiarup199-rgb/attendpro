package com.dec.attendpro

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dec.attendpro.models.UserProfile
import com.dec.attendpro.repository.AuthRepository
import com.dec.attendpro.utils.FaceDataHolder
import com.dec.attendpro.utils.FaceNetHelper
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

class CreateAccountActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    private var selectedImageUri: Uri? = null

    private var centerFace: ByteArray? = null
    private var rightFace: ByteArray? = null
    private var leftFace: ByteArray? = null

    private lateinit var ivIdImage: ImageView
    private lateinit var btnFaceCapture: Button
    private lateinit var llFacePreviews: View
    private lateinit var ivFaceCenter: ImageView
    private lateinit var ivFaceRight: ImageView
    private lateinit var ivFaceLeft: ImageView

    private val pickImage =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {

                selectedImageUri = it

                ivIdImage.setImageURI(it)
            }
        }

    private val captureFaces =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                centerFace = FaceDataHolder.centerFace
                rightFace = FaceDataHolder.rightFace
                leftFace = FaceDataHolder.leftFace

                // Clear memory holder
                FaceDataHolder.clear()

                centerFace?.let {
                    ivFaceCenter.setImageBitmap(
                        BitmapFactory.decodeByteArray(
                            it,
                            0,
                            it.size
                        )
                    )
                }

                rightFace?.let {
                    ivFaceRight.setImageBitmap(
                        BitmapFactory.decodeByteArray(
                            it,
                            0,
                            it.size
                        )
                    )
                }

                leftFace?.let {
                    ivFaceLeft.setImageBitmap(
                        BitmapFactory.decodeByteArray(
                            it,
                            0,
                            it.size
                        )
                    )
                }

                llFacePreviews.visibility = View.VISIBLE

                btnFaceCapture.text = "Completed ✓"

                btnFaceCapture.isEnabled = false

                Toast.makeText(
                    this,
                    "All faces captured successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_account)

        val rgRole =
            findViewById<RadioGroup>(R.id.rgRole)

        val llStudentFields =
            findViewById<LinearLayout>(R.id.llStudentFields)

        val etName =
            findViewById<EditText>(R.id.etName)

        val etEmail =
            findViewById<EditText>(R.id.etEmail)

        val etPassword =
            findViewById<EditText>(R.id.etPassword)

        val etPhone =
            findViewById<EditText>(R.id.etPhone)

        val etRollNumber =
            findViewById<EditText>(R.id.etRollNumber)

        val etSemester =
            findViewById<EditText>(R.id.etSemester)

        val etBranch =
            findViewById<EditText>(R.id.etBranch)

        ivIdImage =
            findViewById(R.id.ivIdImage)

        val btnCreateAccount =
            findViewById<Button>(R.id.btnCreateAccount)

        btnFaceCapture =
            findViewById(R.id.btnFaceCapture)

        llFacePreviews =
            findViewById(R.id.llFacePreviews)

        ivFaceCenter =
            findViewById(R.id.ivFaceCenter)

        ivFaceRight =
            findViewById(R.id.ivFaceRight)

        ivFaceLeft =
            findViewById(R.id.ivFaceLeft)

        val cardIdImage =
            findViewById<View>(R.id.cardIdImage)

        rgRole.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.rbStudent) {

                llStudentFields.visibility = View.VISIBLE

                btnFaceCapture.visibility = View.VISIBLE

            } else {

                llStudentFields.visibility = View.GONE

                btnFaceCapture.visibility = View.GONE
            }
        }

        btnFaceCapture.setOnClickListener {

            val intent =
                Intent(
                    this,
                    FaceCaptureActivity::class.java
                )

            captureFaces.launch(intent)
        }

        cardIdImage.setOnClickListener {

            pickImage.launch("image/*")
        }

        btnCreateAccount.setOnClickListener {

            val name =
                etName.text.toString().trim()

            val email =
                etEmail.text.toString().trim()

            val password =
                etPassword.text.toString().trim()

            val phone =
                etPhone.text.toString().trim()

            val role =
                if (
                    findViewById<RadioButton>(
                        R.id.rbStudent
                    ).isChecked
                ) {
                    "student"
                } else {
                    "teacher"
                }

            if (
                name.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                phone.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please fill all basic details",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val roll =
                etRollNumber.text.toString().trim()

            val sem =
                etSemester.text.toString().trim()

            val branch =
                etBranch.text.toString().trim()

            if (
                role == "student" &&
                (
                        roll.isEmpty() ||
                                sem.isEmpty() ||
                                branch.isEmpty()
                        )
            ) {

                Toast.makeText(
                    this,
                    "Please fill student details",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (
                role == "student" &&
                (
                        centerFace == null ||
                                leftFace == null ||
                                rightFace == null
                        )
            ) {

                Toast.makeText(
                    this,
                    "Please capture all face angles",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            lifecycleScope.launch {

                btnCreateAccount.isEnabled = false

                val idImageBytes =
                    selectedImageUri?.let {
                        uriToBytes(it)
                    }

                // FINAL FACE EMBEDDING
                var faceEmbedding: List<Float>? = null

                if (
                    role == "student" &&
                    centerFace != null &&
                    leftFace != null &&
                    rightFace != null
                ) {

                    try {

                        val faceNetHelper =
                            FaceNetHelper(
                                this@CreateAccountActivity
                            )

                        // Decode bitmaps
                        val centerBitmap =
                            BitmapFactory.decodeByteArray(
                                centerFace,
                                0,
                                centerFace!!.size
                            )

                        val leftBitmap =
                            BitmapFactory.decodeByteArray(
                                leftFace,
                                0,
                                leftFace!!.size
                            )

                        val rightBitmap =
                            BitmapFactory.decodeByteArray(
                                rightFace,
                                0,
                                rightFace!!.size
                            )

                        // Generate embeddings
                        val centerEmbedding =
                            faceNetHelper.getEmbedding(centerBitmap)

                        val leftEmbedding =
                            faceNetHelper.getEmbedding(leftBitmap)

                        val rightEmbedding =
                            faceNetHelper.getEmbedding(rightBitmap)

                        Log.d(
                            "FaceNet",
                            "Center embedding size: ${centerEmbedding.size}"
                        )

                        Log.d(
                            "FaceNet",
                            "Left embedding size: ${leftEmbedding.size}"
                        )

                        Log.d(
                            "FaceNet",
                            "Right embedding size: ${rightEmbedding.size}"
                        )

                        // Average embeddings
                        val averagedEmbedding =
                            FloatArray(centerEmbedding.size)

                        for (i in centerEmbedding.indices) {

                            averagedEmbedding[i] =
                                (
                                        centerEmbedding[i] +
                                                leftEmbedding[i] +
                                                rightEmbedding[i]
                                        ) / 3f
                        }

                        // Normalize embedding
                        var norm = 0f

                        for (value in averagedEmbedding) {
                            norm += value * value
                        }

                        norm = sqrt(norm)

                        if (norm > 0f) {

                            for (i in averagedEmbedding.indices) {
                                averagedEmbedding[i] /= norm
                            }
                        }

                        faceEmbedding =
                            averagedEmbedding.toList()

                        Log.d(
                            "FaceNet",
                            "Final embedding size: ${faceEmbedding.size}"
                        )

                        Log.d(
                            "FaceNet",
                            "Final embedding values: $faceEmbedding"
                        )

                        faceNetHelper.close()

                    } catch (e: Exception) {

                        Log.e(
                            "FaceNet",
                            "Embedding extraction failed",
                            e
                        )
                    }
                }

                val profile =
                    UserProfile(
                        id = "",
                        name = name,
                        email = email,
                        role = role,
                        rollNumber =
                            if (role == "student") roll else null,
                        semester =
                            if (role == "student") sem else null,
                        branch =
                            if (role == "student") branch else null,
                        phoneNumber = phone,
                        faceEmbedding =
                            faceEmbedding ?: emptyList()
                    )

                val result =
                    authRepository.signUp(
                        this@CreateAccountActivity,
                        email,
                        password,
                        profile,
                        idImageBytes,
                        centerFace,
                        rightFace,
                        leftFace
                    )

                if (result.isSuccess) {

                    Toast.makeText(
                        this@CreateAccountActivity,
                        "Account created successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()

                } else {

                    Toast.makeText(
                        this@CreateAccountActivity,
                        "Error: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    btnCreateAccount.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        FaceDataHolder.clear()
    }

    private fun uriToBytes(uri: Uri): ByteArray? {

        return try {

            val bitmap =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.P
                ) {

                    val source =
                        ImageDecoder.createSource(
                            contentResolver,
                            uri
                        )

                    ImageDecoder.decodeBitmap(source)

                } else {

                    MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        uri
                    )
                }

            val stream =
                ByteArrayOutputStream()

            bitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG,
                80,
                stream
            )

            stream.toByteArray()

        } catch (e: Exception) {

            null
        }
    }
}