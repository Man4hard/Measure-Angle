package com.inclinometer.app.ui.bubble

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class BubbleLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pitch = 0f
    private var roll = 0f
    private var isLevel = false

    private val outerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bubbleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.WHITE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }
    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val levelZoneColor = Color.parseColor("#2ECC71")
    private val warningZoneColor = Color.parseColor("#F39C12")
    private val dangerZoneColor = Color.parseColor("#E74C3C")
    private val accentColor = Color.parseColor("#3498DB")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.85f

        val tiltMagnitude = sqrt(pitch * pitch + roll * roll)
        val levelZone = tiltMagnitude < 1f
        val warningZone = tiltMagnitude < 5f

        val ringColor = when {
            levelZone -> levelZoneColor
            warningZone -> warningZoneColor
            else -> dangerZoneColor
        }

        // Background circle
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#1A1A2E")
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Grid lines
        gridPaint.color = Color.parseColor("#2A2A4E")
        for (i in 1..4) {
            val r = radius * i / 5f
            canvas.drawCircle(cx, cy, r, gridPaint)
        }
        canvas.drawLine(cx - radius, cy, cx + radius, cy, gridPaint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, gridPaint)

        // Outer ring
        outerCirclePaint.color = ringColor
        canvas.drawCircle(cx, cy, radius, outerCirclePaint)

        // Inner level zone ring (1 degree indicator)
        innerCirclePaint.color = levelZoneColor
        innerCirclePaint.alpha = 100
        canvas.drawCircle(cx, cy, radius * 0.15f, innerCirclePaint)

        // Crosshair
        crosshairPaint.color = Color.parseColor("#3D3D6D")
        canvas.drawLine(cx - radius, cy, cx + radius, cy, crosshairPaint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, crosshairPaint)

        // Center dot
        canvas.drawCircle(cx, cy, 6f, centerDotPaint)

        // Bubble position — clamp within the circle
        val maxOffset = radius * 0.75f
        val rawOffsetX = (-roll / 45f) * maxOffset
        val rawOffsetY = (-pitch / 45f) * maxOffset
        val dist = sqrt(rawOffsetX * rawOffsetX + rawOffsetY * rawOffsetY)
        val bubbleX: Float
        val bubbleY: Float
        if (dist > maxOffset) {
            bubbleX = cx + rawOffsetX * (maxOffset / dist)
            bubbleY = cy + rawOffsetY * (maxOffset / dist)
        } else {
            bubbleX = cx + rawOffsetX
            bubbleY = cy + rawOffsetY
        }

        // Bubble shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLACK
            alpha = 60
        }
        canvas.drawCircle(bubbleX + 4, bubbleY + 4, radius * 0.12f + 4, shadowPaint)

        // Bubble gradient fill
        val bubbleRadius = radius * 0.12f
        val bubbleGradient = RadialGradient(
            bubbleX - bubbleRadius * 0.3f,
            bubbleY - bubbleRadius * 0.3f,
            bubbleRadius,
            intArrayOf(Color.WHITE, ringColor),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        bubblePaint.shader = bubbleGradient
        canvas.drawCircle(bubbleX, bubbleY, bubbleRadius, bubblePaint)
        canvas.drawCircle(bubbleX, bubbleY, bubbleRadius, bubbleBorderPaint)

        // Degree labels
        textPaint.textSize = radius * 0.09f
        textPaint.color = Color.parseColor("#8888AA")
        canvas.drawText("45°", cx + radius * 0.56f, cy + textPaint.textSize * 0.4f, textPaint)
        canvas.drawText("-45°", cx - radius * 0.56f, cy + textPaint.textSize * 0.4f, textPaint)
    }

    fun updateAngles(pitch: Float, roll: Float) {
        this.pitch = pitch
        this.roll = roll
        val tiltMagnitude = sqrt(pitch * pitch + roll * roll)
        this.isLevel = tiltMagnitude < 0.5f
        invalidate()
    }
}
