package com.dec.attendpro.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(private val context: Context) {

    private var cameraExecutor: ExecutorService =
        Executors.newSingleThreadExecutor()

    private var isProcessingFrame = false

    private var lastProcessedTime = 0L

    companion object {
        private const val TAG = "CameraManager"

        // Process only every 1.5 seconds
        private const val PROCESS_INTERVAL = 1500L

        // Minimum face size for recognition
        private const val MIN_FACE_SIZE = 160
    }

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .enableTracking()
            .build()
    )

    fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        useFrontCamera: Boolean,
        onFacesDetected: (List<Face>, Int, Int) -> Unit,
        onFaceCropped: (Bitmap) -> Unit
    ) {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
                return@addListener
            }

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
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

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(
                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                )
                .setResolutionSelector(resolutionSelector)
                .setOutputImageFormat(
                    ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
                )
                .build()
                .also {

                    it.setAnalyzer(cameraExecutor) { imageProxy ->

                        val currentTime =
                            System.currentTimeMillis()

                        // Prevent frame flooding
                        if (currentTime - lastProcessedTime <
                            PROCESS_INTERVAL
                        ) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        // Prevent overlapping processing
                        if (isProcessingFrame) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        lastProcessedTime = currentTime
                        isProcessingFrame = true

                        processImageProxy(
                            imageProxy,
                            onFacesDetected,
                            onFaceCropped
                        )
                    }
                }

            val cameraSelector =
                if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

            try {

                // VERY IMPORTANT
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

                Log.d(TAG, "Camera started successfully")

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        imageProxy: ImageProxy,
        onFacesDetected: (List<Face>, Int, Int) -> Unit,
        onFaceCropped: (Bitmap) -> Unit
    ) {

        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            isProcessingFrame = false
            return
        }

        try {

            val rotationDegrees =
                imageProxy.imageInfo.rotationDegrees

            val image = InputImage.fromMediaImage(
                mediaImage,
                rotationDegrees
            )

            val width =
                if (rotationDegrees == 90 ||
                    rotationDegrees == 270
                ) {
                    imageProxy.height
                } else {
                    imageProxy.width
                }

            val height =
                if (rotationDegrees == 90 ||
                    rotationDegrees == 270
                ) {
                    imageProxy.width
                } else {
                    imageProxy.height
                }

            faceDetector.process(image)
                .addOnSuccessListener { faces ->

                    onFacesDetected(faces, width, height)

                    if (faces.isEmpty()) {
                        return@addOnSuccessListener
                    }

                    try {

                        val face = faces[0]
                        val boundingBox = face.boundingBox

                        // Reject tiny faces
                        if (boundingBox.width() < MIN_FACE_SIZE ||
                            boundingBox.height() < MIN_FACE_SIZE
                        ) {

                            Log.d(
                                TAG,
                                "Face too small for recognition"
                            )

                            return@addOnSuccessListener
                        }

                        var bitmap = imageProxy.toBitmap()

                        // Rotate bitmap
                        if (rotationDegrees != 0) {

                            val matrix = Matrix()

                            matrix.postRotate(
                                rotationDegrees.toFloat()
                            )

                            bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            )
                        }

                        val left =
                            boundingBox.left.coerceAtLeast(0)

                        val top =
                            boundingBox.top.coerceAtLeast(0)

                        val right =
                            boundingBox.right.coerceAtMost(
                                bitmap.width
                            )

                        val bottom =
                            boundingBox.bottom.coerceAtMost(
                                bitmap.height
                            )

                        val cropWidth = right - left
                        val cropHeight = bottom - top

                        if (cropWidth <= 0 ||
                            cropHeight <= 0
                        ) {
                            return@addOnSuccessListener
                        }

                        val croppedFace =
                            Bitmap.createBitmap(
                                bitmap,
                                left,
                                top,
                                cropWidth,
                                cropHeight
                            )

                        Log.d(
                            TAG,
                            "Face cropped successfully: " +
                                    "${croppedFace.width}x${croppedFace.height}"
                        )

                        onFaceCropped(croppedFace)

                    } catch (e: Exception) {

                        Log.e(
                            TAG,
                            "Bitmap processing failed",
                            e
                        )
                    }
                }

                .addOnFailureListener { e ->

                    Log.e(
                        TAG,
                        "Face detection failed",
                        e
                    )
                }

                .addOnCompleteListener {

                    imageProxy.close()
                    isProcessingFrame = false
                }

        } catch (e: Exception) {

            Log.e(TAG, "Processing failed", e)

            imageProxy.close()
            isProcessingFrame = false
        }
    }

    fun shutdown() {

        try {

            faceDetector.close()

            if (!cameraExecutor.isShutdown) {
                cameraExecutor.shutdown()
            }

            Log.d(TAG, "CameraManager shutdown complete")

        } catch (e: Exception) {

            Log.e(TAG, "Shutdown failed", e)
        }
    }
}