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

    // Colors
    private val bgColor      = Color.parseColor("#1A1A2E")
    private val tubeColor    = Color.parseColor("#0D2137")
    private val tubeBorder   = Color.parseColor("#00D4FF")
    private val levelGreen   = Color.parseColor("#00FF88")
    private val warningOrange= Color.parseColor("#FF9900")
    private val dangerRed    = Color.parseColor("#FF3355")
    private val tickColor    = Color.parseColor("#4488AA")
    private val textColor    = Color.parseColor("#CCDDEE")
    private val glassHighlight = Color.parseColor("#33FFFFFF")

    // Paints
    private val bgPaint       = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tubeBgPaint   = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tubeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tickPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
    private val smallTextPaint= Paint(Paint.ANTI_ALIAS_FLAG)
    private val glassPaint    = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        bgPaint.color = bgColor
        bgPaint.style = Paint.Style.FILL

        tubeBgPaint.style = Paint.Style.FILL

        tubeBorderPaint.style = Paint.Style.STROKE
        tubeBorderPaint.strokeWidth = 3f

        tickPaint.color = tickColor
        tickPaint.style = Paint.Style.STROKE
        tickPaint.strokeWidth = 2f

        textPaint.color = textColor
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.textAlign = Paint.Align.CENTER

        smallTextPaint.color = Color.parseColor("#7799BB")
        smallTextPaint.typeface = Typeface.MONOSPACE
        smallTextPaint.textAlign = Paint.Align.CENTER

        glassPaint.style = Paint.Style.FILL
        glassPaint.color = glassHighlight

        centerLinePaint.color = Color.parseColor("#FF444466")
        centerLinePaint.style = Paint.Style.STROKE
        centerLinePaint.strokeWidth = 2f
        centerLinePaint.pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // Background
        bgPaint.color = bgColor
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val tilt = sqrt(pitch * pitch + roll * roll)
        val bubbleColor = when {
            tilt < 0.5f -> levelGreen
            tilt < 3f   -> warningOrange
            else        -> dangerRed
        }

        // ── HORIZONTAL SPIRIT LEVEL TUBE (roll) ──
        val hTubeH = h * 0.22f
        val hTubeY = h * 0.14f
        val hTubeLeft  = w * 0.07f
        val hTubeRight = w * 0.93f
        val hTubeCy = hTubeY + hTubeH / 2f
        val hTubeR = RectF(hTubeLeft, hTubeY, hTubeRight, hTubeY + hTubeH)
        val cornerR = hTubeH / 2f

        // Tube background
        tubeBgPaint.color = tubeColor
        canvas.drawRoundRect(hTubeCenterZone(hTubeLeft, hTubeY, hTubeRight, hTubeY + hTubeH, cornerR), tubeBgPaint)
        canvas.drawRoundRect(hTubeCenterZone(hTubeLeft, hTubeY, hTubeRight, hTubeY + hTubeH, cornerR), tubeBorderPaint.apply { color = tubeBorder; strokeWidth = 2.5f })

        // Tick marks on horizontal tube
        drawHorizontalTicks(canvas, hTubeLeft, hTubeRight, hTubeCy, hTubeH)

        // Center zone (level zone) – green band
        val levelZoneWidth = (hTubeRight - hTubeLeft) * 0.07f
        val centerX = (hTubeLeft + hTubeRight) / 2f
        val levelZonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2200FF88")
            style = Paint.Style.FILL
        }
        canvas.drawRect(centerX - levelZoneWidth / 2f, hTubeY + 2, centerX + levelZoneWidth / 2f, hTubeY + hTubeH - 2, levelZonePaint)

        // Center dashed line
        canvas.drawLine(centerX, hTubeY + 4, centerX, hTubeY + hTubeH - 4, centerLinePaint)

        // BUBBLE on horizontal tube (roll controls X position)
        val maxBubbleTravelH = (hTubeRight - hTubeLeft) * 0.44f
        val bubbleX = centerX + (-roll / 45f * maxBubbleTravelH).coerceIn(-maxBubbleTravelH, maxBubbleTravelH)
        val bubbleRadius = hTubeH * 0.32f
        drawBubble(canvas, bubbleX, hTubeCy, bubbleRadius, bubbleColor)

        // Glass sheen on top
        glassPaint.color = glassHighlight
        canvas.drawRoundRect(
            RectF(hTubeLeft + 4, hTubeY + 4, hTubeRight - 4, hTubeCy),
            cornerR - 4, cornerR - 4, glassPaint
        )

        // Label
        smallTextPaint.textSize = h * 0.045f
        canvas.drawText("ROLL (SIDE TO SIDE)", centerX, hTubeY - 10f, smallTextPaint)

        // ── VERTICAL SPIRIT LEVEL TUBE (pitch) ──
        val vTubeW = w * 0.10f
        val vTubeTop = h * 0.44f
        val vTubeBot = h * 0.96f
        val vTubeX  = w * 0.08f
        val vTubeCx = vTubeX + vTubeW / 2f
        val vCornerR = vTubeW / 2f

        tubeBgPaint.color = tubeColor
        canvas.drawRoundRect(RectF(vTubeX, vTubeTop, vTubeX + vTubeW, vTubeBot), vCornerR, vCornerR, tubeBgPaint)
        canvas.drawRoundRect(RectF(vTubeX, vTubeTop, vTubeX + vTubeW, vTubeBot), vCornerR, vCornerR,
            tubeBorderPaint.apply { color = tubeBorder; strokeWidth = 2.5f })

        // Ticks on vertical tube
        drawVerticalTicks(canvas, vTubeX, vTubeX + vTubeW, vTubeTop, vTubeBot, vTubeCx)

        // Center zone of vertical tube
        val vCenterY = (vTubeTop + vTubeBot) / 2f
        val levelZoneH = (vTubeBot - vTubeTop) * 0.07f
        canvas.drawRect(vTubeX + 2, vCenterY - levelZoneH / 2f, vTubeX + vTubeW - 2, vCenterY + levelZoneH / 2f, levelZonePaint)
        canvas.drawLine(vTubeX + 4, vCenterY, vTubeX + vTubeW - 4, vCenterY, centerLinePaint)

        // BUBBLE on vertical tube (pitch controls Y position)
        val maxBubbleTravelV = (vTubeBot - vTubeTop) * 0.44f
        val bubbleY = vCenterY + (pitch / 45f * maxBubbleTravelV).coerceIn(-maxBubbleTravelV, maxBubbleTravelV)
        val vBubbleR = vTubeW * 0.32f
        drawBubble(canvas, vTubeCx, bubbleY, vBubbleR, bubbleColor)

        // Glass sheen vertical tube
        canvas.drawRoundRect(RectF(vTubeX + 4, vTubeTop + 4, vTubeCx, vTubeBot - 4), vCornerR - 4, vCornerR - 4, glassPaint)

        // Label
        canvas.save()
        canvas.rotate(-90f, vTubeCx, vTubeTop - 14f)
        canvas.drawText("PITCH", vTubeCx, vTubeTop - 14f, smallTextPaint)
        canvas.restore()

        // ── CIRCULAR VIAL (center) ──
        val circleCx = w * 0.5f + (w * 0.07f)
        val circleCy = h * 0.70f
        val circleR  = minOf(w * 0.18f, h * 0.22f)
        drawCircularVial(canvas, circleCx, circleCy, circleR, pitch, roll, bubbleColor)

        // ── DEGREE READOUTS ──
        drawDegreeReadouts(canvas, w, h, pitch, roll, tilt, bubbleColor)
    }

    private fun hTubeCenterZone(l: Float, t: Float, r: Float, b: Float, cr: Float): RectF = RectF(l, t, r, b)

    private fun drawBubble(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        // Shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = Color.argb(80, 0, 0, 0)
        }
        canvas.drawCircle(cx + 3f, cy + 3f, r + 2f, shadowPaint)

        // Bubble gradient
        val gradient = RadialGradient(
            cx - r * 0.3f, cy - r * 0.3f, r * 1.2f,
            intArrayOf(Color.WHITE, color, Color.parseColor("#66000000")),
            floatArrayOf(0.0f, 0.5f, 1.0f),
            Shader.TileMode.CLAMP
        )
        val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = gradient
        }
        canvas.drawCircle(cx, cy, r, bubblePaint)

        // Rim
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            this.color = Color.argb(180, 255, 255, 255)
        }
        canvas.drawCircle(cx, cy, r, rimPaint)

        // Specular highlight
        val specPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = Color.argb(160, 255, 255, 255)
        }
        canvas.drawCircle(cx - r * 0.3f, cy - r * 0.35f, r * 0.22f, specPaint)
    }

    private fun drawHorizontalTicks(canvas: Canvas, l: Float, r: Float, cy: Float, h: Float) {
        val center = (l + r) / 2f
        val totalW = r - l
        val degreesPerPx = 45f / (totalW * 0.44f)
        val tickSpacingPx = 1f / degreesPerPx
        tickPaint.textAlign = Paint.Align.CENTER
        for (i in -9..9) {
            val x = center + i * tickSpacingPx * 5f
            if (x < l + 8 || x > r - 8) continue
            val isMajor = i % 3 == 0
            val tickH = if (isMajor) h * 0.42f else h * 0.25f
            tickPaint.strokeWidth = if (isMajor) 2.5f else 1.5f
            tickPaint.color = if (i == 0) Color.parseColor("#00FF88") else tickColor
            canvas.drawLine(x, cy - tickH / 2f, x, cy + tickH / 2f, tickPaint)
        }
    }

    private fun drawVerticalTicks(canvas: Canvas, l: Float, r: Float, top: Float, bot: Float, cx: Float) {
        val center = (top + bot) / 2f
        val totalH = bot - top
        val tickSpacingPx = totalH * 0.44f / 9f
        for (i in -9..9) {
            val y = center + i * tickSpacingPx
            if (y < top + 8 || y > bot - 8) continue
            val isMajor = i % 3 == 0
            val tw = (r - l) * if (isMajor) 0.42f else 0.25f
            tickPaint.strokeWidth = if (isMajor) 2.5f else 1.5f
            tickPaint.color = if (i == 0) Color.parseColor("#00FF88") else tickColor
            canvas.drawLine(l + (r - l) / 2f - tw / 2f, y, l + (r - l) / 2f + tw / 2f, y, tickPaint)
        }
    }

    private fun drawCircularVial(canvas: Canvas, cx: Float, cy: Float, r: Float, pitch: Float, roll: Float, bubbleColor: Int) {
        // Outer ring
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = tubeColor
        }
        canvas.drawCircle(cx, cy, r, outerPaint)
        canvas.drawCircle(cx, cy, r, tubeBorderPaint.apply { color = tubeBorder; strokeWidth = 3f; style = Paint.Style.STROKE })

        // Concentric rings for scale
        for (frac in listOf(0.33f, 0.66f)) {
            val rp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; strokeWidth = 1f; color = tickColor
            }
            canvas.drawCircle(cx, cy, r * frac, rp)
        }

        // Crosshairs
        val chPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 1.5f; color = tickColor
        }
        canvas.drawLine(cx - r, cy, cx + r, cy, chPaint)
        canvas.drawLine(cx, cy - r, cx, cy + r, chPaint)

        // Center dot (target)
        val cdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = levelGreen }
        canvas.drawCircle(cx, cy, r * 0.05f, cdPaint)

        // Bubble in circular vial
        val maxOff = r * 0.75f
        val rawX = (-roll / 45f) * maxOff
        val rawY = (-pitch / 45f) * maxOff
        val dist = sqrt(rawX * rawX + rawY * rawY)
        val bx: Float; val by: Float
        if (dist > maxOff) { bx = cx + rawX * maxOff / dist; by = cy + rawY * maxOff / dist }
        else { bx = cx + rawX; by = cy + rawY }
        drawBubble(canvas, bx, by, r * 0.15f, bubbleColor)

        // Glass sheen
        val sheen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL; color = glassHighlight
            shader = RadialGradient(cx - r * 0.3f, cy - r * 0.3f, r * 0.7f,
                intArrayOf(Color.argb(60, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, r, sheen)
    }

    private fun drawDegreeReadouts(canvas: Canvas, w: Float, h: Float, pitch: Float, roll: Float, tilt: Float, bubbleColor: Int) {
        val readoutArea = RectF(w * 0.30f, h * 0.44f, w * 0.98f, h * 0.98f)

        // Pitch card
        val pitchCard = RectF(readoutArea.left, readoutArea.top, readoutArea.centerX() - 6f, readoutArea.centerY() - 6f)
        drawAngleCard(canvas, pitchCard, "PITCH", pitch, Color.parseColor("#FF00D4FF"), bubbleColor)

        // Roll card
        val rollCard = RectF(readoutArea.centerX() + 6f, readoutArea.top, readoutArea.right, readoutArea.centerY() - 6f)
        drawAngleCard(canvas, rollCard, "ROLL", roll, Color.parseColor("#FFA8C8FF"), bubbleColor)

        // Status bar
        val statusY = readoutArea.centerY() + 6f
        val statusH = readoutArea.bottom - statusY
        val statusRect = RectF(readoutArea.left, statusY, readoutArea.right, statusY + statusH)
        drawStatusBar(canvas, statusRect, tilt, bubbleColor)
    }

    private fun drawAngleCard(canvas: Canvas, rect: RectF, label: String, value: Float, labelColor: Int, bubbleColor: Int) {
        // Card bg
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#22FFFFFF")
        }
        canvas.drawRoundRect(rect, 16f, 16f, cardPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 1.5f; color = Color.parseColor("#33FFFFFF")
        }
        canvas.drawRoundRect(rect, 16f, 16f, borderPaint)

        val cx = rect.centerX()

        // Label
        smallTextPaint.color = labelColor
        smallTextPaint.textSize = rect.height() * 0.16f
        canvas.drawText(label, cx, rect.top + rect.height() * 0.22f, smallTextPaint)

        // Big degree value
        textPaint.color = bubbleColor
        textPaint.textSize = rect.height() * 0.46f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("%.1f°".format(value), cx, rect.top + rect.height() * 0.70f, textPaint)

        // Abs direction label
        val dir = when {
            value > 0.3f  -> if (label == "PITCH") "▲ UP" else "◀ LEFT"
            value < -0.3f -> if (label == "PITCH") "▼ DOWN" else "RIGHT ▶"
            else           -> "● LEVEL"
        }
        smallTextPaint.color = Color.parseColor("#88AABBCC")
        smallTextPaint.textSize = rect.height() * 0.13f
        canvas.drawText(dir, cx, rect.top + rect.height() * 0.88f, smallTextPaint)
    }

    private fun drawStatusBar(canvas: Canvas, rect: RectF, tilt: Float, bubbleColor: Int) {
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL; color = Color.parseColor("#22FFFFFF")
        }
        canvas.drawRoundRect(rect, 16f, 16f, cardPaint)

        val cx = rect.centerX()
        val cy = rect.centerY()

        val statusText = when {
            tilt < 0.5f -> "✓  LEVEL"
            tilt < 2f   -> "NEARLY LEVEL"
            tilt < 5f   -> "SLIGHT TILT"
            else        -> "NOT LEVEL"
        }
        textPaint.color = bubbleColor
        textPaint.textSize = rect.height() * 0.42f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(statusText, cx, cy + textPaint.textSize * 0.36f, textPaint)

        smallTextPaint.color = Color.parseColor("#66AABBCC")
        smallTextPaint.textSize = rect.height() * 0.22f
        canvas.drawText("%.2f° total tilt".format(tilt), cx, rect.bottom - rect.height() * 0.15f, smallTextPaint)
    }

    fun updateAngles(pitch: Float, roll: Float) {
        this.pitch = pitch
        this.roll = roll
        invalidate()
    }
}
