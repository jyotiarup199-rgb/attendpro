package com.dec.attendpro.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class FaceNetHelper(context: Context) {

    private var interpreter: Interpreter? = null

    private val modelName = "mobile_face_net.tflite"

    // Dynamic model config
    private var imgHeight = 160
    private var imgWidth = 160
    private var isGrayscale = false
    private var embeddingSize = 192

    init {

        try {

            val options = Interpreter.Options()
            options.setNumThreads(4)

            interpreter = Interpreter(
                loadModelFile(context, modelName),
                options
            )

            interpreter?.let { interp ->

                val inputTensor =
                    interp.getInputTensor(0)

                val inputShape =
                    inputTensor.shape()

                if (inputShape.size >= 4) {

                    imgHeight = inputShape[1]
                    imgWidth = inputShape[2]

                    isGrayscale =
                        inputShape[3] == 1
                }

                val outputTensor =
                    interp.getOutputTensor(0)

                val outputShape =
                    outputTensor.shape()

                Log.d(
                    "FaceNetHelper",
                    "TFLite Model loaded successfully"
                )

                Log.d(
                    "FaceNetHelper",
                    "Input shape: ${
                        inputShape.joinToString()
                    }"
                )

                Log.d(
                    "FaceNetHelper",
                    "Output shape: ${
                        outputShape.joinToString()
                    }"
                )

                Log.d(
                    "FaceNetHelper",
                    "Detected config: " +
                            "${imgHeight}x${imgWidth}, " +
                            "Gray=$isGrayscale"
                )
            }

        } catch (e: Exception) {

            Log.e(
                "FaceNetHelper",
                "Error loading TFLite model",
                e
            )
        }
    }

    private fun loadModelFile(
        context: Context,
        modelName: String
    ): MappedByteBuffer {

        val fileDescriptor =
            context.assets.openFd(modelName)

        val inputStream =
            FileInputStream(fileDescriptor.fileDescriptor)

        val fileChannel =
            inputStream.channel

        val startOffset =
            fileDescriptor.startOffset

        val declaredLength =
            fileDescriptor.declaredLength

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {

        if (interpreter == null) {

            Log.e(
                "FaceNetHelper",
                "Interpreter is null"
            )

            return FloatArray(192)
        }

        try {

            Log.d(
                "FaceNetHelper",
                "Bitmap size: ${bitmap.width}x${bitmap.height}"
            )

            val imageProcessorBuilder =
                ImageProcessor.Builder()

                    .add(
                        ResizeOp(
                            imgHeight,
                            imgWidth,
                            ResizeOp.ResizeMethod.BILINEAR
                        )
                    )

            if (isGrayscale) {

                imageProcessorBuilder.add(
                    TransformToGrayscaleOp()
                )
            }

            val imageProcessor =
                imageProcessorBuilder

                    // MobileFaceNet expects [-1,1]
                    .add(
                        NormalizeOp(
                            127.5f,
                            127.5f
                        )
                    )

                    .build()

            var tensorImage =
                TensorImage(
                    org.tensorflow.lite.DataType.FLOAT32
                )

            tensorImage.load(bitmap)

            tensorImage =
                imageProcessor.process(tensorImage)

            val outputTensor =
                interpreter!!.getOutputTensor(0)

            val outputShape =
                outputTensor.shape()

            Log.d(
                "FaceNetHelper",
                "Runtime output shape: ${
                    outputShape.joinToString()
                }"
            )

            val embedding: FloatArray =
                when (outputShape.size) {

                    2 -> {

                        // Example: [1,192]

                        val output =
                            Array(1) {
                                FloatArray(outputShape[1])
                            }

                        interpreter?.run(
                            tensorImage.buffer,
                            output
                        )

                        output[0]
                    }

                    4 -> {

                        // Example: [1,1,1,192]

                        val output =
                            Array(1) {
                                Array(outputShape[1]) {
                                    Array(outputShape[2]) {
                                        FloatArray(outputShape[3])
                                    }
                                }
                            }

                        interpreter?.run(
                            tensorImage.buffer,
                            output
                        )

                        output[0][0][0]
                    }

                    else -> {

                        Log.e(
                            "FaceNetHelper",
                            "Unsupported output shape"
                        )

                        return FloatArray(192)
                    }
                }

            val normalizedEmbedding =
                l2Normalize(embedding)

            Log.d(
                "FaceNetHelper",
                "Inference completed"
            )

            Log.d(
                "FaceNetHelper",
                "Embedding size: ${
                    normalizedEmbedding.size
                }"
            )

            Log.d(
                "FaceNetHelper",
                "MIN: ${
                    normalizedEmbedding.minOrNull()
                }"
            )

            Log.d(
                "FaceNetHelper",
                "MAX: ${
                    normalizedEmbedding.maxOrNull()
                }"
            )

            Log.d(
                "FaceNetHelper",
                "Embedding values: ${
                    normalizedEmbedding.joinToString()
                }"
            )

            val allZeros =
                normalizedEmbedding.all { it == 0f }

            if (allZeros) {

                Log.e(
                    "FaceNetHelper",
                    "WARNING: ALL EMBEDDINGS ARE ZERO"
                )
            }

            return normalizedEmbedding

        } catch (e: Exception) {

            Log.e(
                "FaceNetHelper",
                "Inference failed",
                e
            )

            return FloatArray(192)
        }
    }

    private fun l2Normalize(
        embedding: FloatArray
    ): FloatArray {

        var sum = 0f

        for (value in embedding) {
            sum += value * value
        }

        val magnitude = sqrt(sum)

        if (magnitude == 0f) {
            return embedding
        }

        return embedding.map {
            it / magnitude
        }.toFloatArray()
    }

    fun close() {

        try {

            interpreter?.close()

            Log.d(
                "FaceNetHelper",
                "Interpreter closed"
            )

        } catch (e: Exception) {

            Log.e(
                "FaceNetHelper",
                "Error closing interpreter",
                e
            )
        }
    }
}