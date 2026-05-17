package com.dec.attendpro

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dec.attendpro.ui.views.FaceOverlayView
import com.dec.attendpro.utils.FaceDataHolder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceCaptureActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlay: FaceOverlayView
    private lateinit var btnCapture: Button
    private lateinit var tvInstruction: TextView
    private lateinit var tvStep: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var ivPreview: ImageView

    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null

    private var currentStep = 1

    private val capturedImages = mutableListOf<ByteArray>()

    private var lastDetectedFace: Rect? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_face_capture)

        previewView = findViewById(R.id.previewView)
        overlay = findViewById(R.id.overlay)
        btnCapture = findViewById(R.id.btnCapture)
        tvInstruction = findViewById(R.id.tvInstruction)
        tvStep = findViewById(R.id.tvStep)
        progressBar = findViewById(R.id.progressBar)
        ivPreview = findViewById(R.id.ivPreview)

        cameraExecutor =
            Executors.newSingleThreadExecutor()

        btnCapture.setOnClickListener {
            takePhoto()
        }

        if (allPermissionsGranted()) {

            startCamera()

        } else {

            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        updateUI()
    }

    private fun allPermissionsGranted(): Boolean {

        return REQUIRED_PERMISSIONS.all {

            ContextCompat.checkSelfPermission(
                baseContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            val preview =
                Preview.Builder()
                    .build()
                    .also {

                        it.setSurfaceProvider(
                            previewView.surfaceProvider
                        )
                    }

            val resolutionSelector =
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(640, 480),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

            imageCapture =
                ImageCapture.Builder()
                    .setResolutionSelector(
                        resolutionSelector
                    )
                    .build()

            val detectorOptions =
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(
                        FaceDetectorOptions.PERFORMANCE_MODE_FAST
                    )
                    .build()

            val detector =
                FaceDetection.getClient(detectorOptions)

            val imageAnalyzer =
                ImageAnalysis.Builder()
                    .build()
                    .also {

                        it.setAnalyzer(cameraExecutor) { imageProxy ->

                            processImageProxy(
                                detector,
                                imageProxy
                            )
                        }
                    }

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )

            } catch (e: Exception) {

                Log.e(
                    "FaceCapture",
                    "Camera bind failed",
                    e
                )
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        detector: FaceDetector,
        imageProxy: ImageProxy
    ) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {

            val image =
                InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

            detector.process(image)
                .addOnSuccessListener { faces ->

                    var state =
                        FaceOverlayView.State.NONE

                    if (faces.isNotEmpty()) {

                        val face = faces[0]

                        lastDetectedFace =
                            face.boundingBox

                        val angle =
                            face.headEulerAngleY

                        val correct =
                            when (currentStep) {

                                // CENTER
                                1 ->
                                    kotlin.math.abs(angle) < 25

                                // RIGHT TURN
                                2 ->
                                    angle < -10

                                // LEFT TURN
                                3 ->
                                    angle > 10

                                else -> true
                            }

                        state =
                            if (correct) {
                                FaceOverlayView.State.CORRECT
                            } else {
                                FaceOverlayView.State.WRONG
                            }
                    }

                    runOnUiThread {

                        overlay.setState(state)

                        btnCapture.isEnabled =
                            state ==
                                    FaceOverlayView.State.CORRECT
                    }
                }
                .addOnCompleteListener {

                    imageProxy.close()
                }
        } else {

            imageProxy.close()
        }
    }

    private fun takePhoto() {

        val imageCapture =
            imageCapture ?: return

        progressBar.visibility = View.VISIBLE

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),

            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(
                    image: ImageProxy
                ) {

                    try {

                        val bitmap =
                            imageProxyToBitmap(image)

                        val cropped =
                            cropFace(bitmap)

                        val bytes =
                            bitmapToByteArray(cropped)

                        capturedImages.add(bytes)

                        ivPreview.setImageBitmap(cropped)

                        image.close()

                        if (currentStep < 3) {

                            currentStep++

                            updateUI()

                            progressBar.visibility =
                                View.GONE

                        } else {

                            FaceDataHolder.centerFace =
                                capturedImages[0]

                            FaceDataHolder.rightFace =
                                capturedImages[1]

                            FaceDataHolder.leftFace =
                                capturedImages[2]

                            setResult(Activity.RESULT_OK)

                            finish()
                        }

                    } catch (e: Exception) {

                        image.close()

                        progressBar.visibility =
                            View.GONE

                        Log.e(
                            "FaceCapture",
                            "Capture failed",
                            e
                        )
                    }
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    progressBar.visibility =
                        View.GONE

                    Log.e(
                        "FaceCapture",
                        "Capture error",
                        exception
                    )
                }
            }
        )
    }

    private fun cropFace(bitmap: Bitmap): Bitmap {

        val rect =
            lastDetectedFace ?: return bitmap

        val safeRect =
            Rect(
                rect.left.coerceAtLeast(0),
                rect.top.coerceAtLeast(0),
                rect.right.coerceAtMost(bitmap.width),
                rect.bottom.coerceAtMost(bitmap.height)
            )

        return Bitmap.createBitmap(
            bitmap,
            safeRect.left,
            safeRect.top,
            safeRect.width(),
            safeRect.height()
        )
    }

    private fun updateUI() {

        when (currentStep) {

            1 -> {

                tvInstruction.text =
                    "Look Straight"

                tvStep.text =
                    "Step 1/3"
            }

            2 -> {

                tvInstruction.text =
                    "Turn Right"

                tvStep.text =
                    "Step 2/3"
            }

            3 -> {

                tvInstruction.text =
                    "Turn Left"

                tvStep.text =
                    "Step 3/3"
            }
        }
    }

    private fun imageProxyToBitmap(
        image: ImageProxy
    ): Bitmap {

        val planeBuffer =
            image.planes[0].buffer

        val bytes =
            ByteArray(planeBuffer.remaining())

        planeBuffer.get(bytes)

        var bitmap =
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )

        // Fallback if decode fails
        if (bitmap == null) {

            bitmap = Bitmap.createBitmap(
                image.width,
                image.height,
                Bitmap.Config.ARGB_8888
            )
        }

        val matrix = Matrix()

        matrix.postRotate(
            image.imageInfo.rotationDegrees.toFloat()
        )

        // Mirror front camera
        matrix.postScale(
            -1f,
            1f,
            bitmap.width / 2f,
            bitmap.height / 2f
        )

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }



    private fun bitmapToByteArray(
        bitmap: Bitmap
    ): ByteArray {

        val stream =
            ByteArrayOutputStream()

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            stream
        )

        return stream.toByteArray()
    }

    override fun onDestroy() {

        super.onDestroy()

        cameraExecutor.shutdown()
    }

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA
            )
    }
}