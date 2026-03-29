package com.inclinometer.app.ui.camera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CameraOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pitch = 0f
    private var roll = 0f

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        color = Color.argb(100, 255, 255, 255)
    }
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(200, 0, 255, 100)
    }
    private val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.argb(220, 255, 200, 0)
    }
    private val centerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(200, 0, 255, 100)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f

        // Grid lines
        val cols = 3
        val rows = 4
        for (i in 1 until cols) {
            val x = w * i / cols
            canvas.drawLine(x, 0f, x, h, gridPaint)
        }
        for (i in 1 until rows) {
            val y = h * i / rows
            canvas.drawLine(0f, y, w, y, gridPaint)
        }

        // Center crosshair
        val crossSize = 80f
        canvas.drawLine(cx - crossSize, cy, cx - 20f, cy, crosshairPaint)
        canvas.drawLine(cx + 20f, cy, cx + crossSize, cy, crosshairPaint)
        canvas.drawLine(cx, cy - crossSize, cx, cy - 20f, crosshairPaint)
        canvas.drawLine(cx, cy + 20f, cx, cy + crossSize, crosshairPaint)
        canvas.drawCircle(cx, cy, 24f, centerCirclePaint)

        // Horizon line - rotates with roll
        canvas.save()
        canvas.rotate(-roll, cx, cy)
        val horizonY = cy + (pitch / 90f) * (h / 4f)
        canvas.drawLine(cx - w * 0.4f, horizonY, cx + w * 0.4f, horizonY, horizonPaint)
        // Wing markers
        horizonPaint.strokeWidth = 8f
        canvas.drawLine(cx - w * 0.4f, horizonY, cx - w * 0.4f + 30f, horizonY, horizonPaint)
        canvas.drawLine(cx + w * 0.4f - 30f, horizonY, cx + w * 0.4f, horizonY, horizonPaint)
        horizonPaint.strokeWidth = 4f
        canvas.restore()
    }

    fun updateAngles(pitch: Float, roll: Float) {
        this.pitch = pitch
        this.roll = roll
        invalidate()
    }
}
