package com.dec.attendpro.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
        alpha = 150
        style = Paint.Style.FILL
    }

    private val eraserPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    enum class State {
        NONE, WRONG, CORRECT
    }

    private var currentState = State.NONE

    fun setState(state: State) {
        currentState = state
        borderPaint.color = when (state) {
            State.NONE -> Color.WHITE
            State.WRONG -> Color.RED
            State.CORRECT -> Color.GREEN
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val centerX = width / 2f
        val centerY = height / 2.5f
        val radius = width * 0.35f

        canvas.drawCircle(centerX, centerY, radius, eraserPaint)
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        canvas.restoreToCount(layer)
    }
}
