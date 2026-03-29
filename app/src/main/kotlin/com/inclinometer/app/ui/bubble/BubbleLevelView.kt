package com.inclinometer.app.ui.bubble

  import android.content.Context
  import android.graphics.*
  import android.util.AttributeSet
  import android.view.MotionEvent
  import android.view.View
  import kotlin.math.*

  class BubbleLevelView @JvmOverloads constructor(
      context: Context,
      attrs: AttributeSet? = null,
      defStyleAttr: Int = 0
  ) : View(context, attrs, defStyleAttr) {

      private var pitch = 0f  // Y axis (forward/back tilt)
      private var roll  = 0f  // X axis (left/right tilt)

      // Callback for button taps
      var onCalibrateClick: (() -> Unit)? = null
      var onSoundClick:     (() -> Unit)? = null
      var onLockClick:      (() -> Unit)? = null
      var onSettingsClick:  (() -> Unit)? = null
      var isSoundEnabled = true
      var isLocked = false

      // Button rects for hit-testing
      private val btnCalibrate = RectF()
      private val btnSound     = RectF()
      private val btnLock      = RectF()
      private val btnSettings  = RectF()

      // ── Colours ──────────────────────────────────────────────────────────────
      private val bgDark      = Color.parseColor("#111111")
      private val limeA       = Color.parseColor("#AADD00")
      private val limeB       = Color.parseColor("#668800")
      private val limeGlass   = Color.parseColor("#CCFF44")
      private val limeDeep    = Color.parseColor("#334400")
      private val rimDark     = Color.parseColor("#1C1C1C")
      private val rimGray     = Color.parseColor("#3A3A3A")
      private val rimLight    = Color.parseColor("#555555")
      private val stitchColor = Color.parseColor("#444444")
      private val lcdBg       = Color.parseColor("#0B1500")
      private val lcdText     = Color.parseColor("#88FF00")
      private val lcdDim      = Color.parseColor("#224400")
      private val btnBg       = Color.parseColor("#222222")
      private val btnRim      = Color.parseColor("#404040")
      private val iconColor   = Color.parseColor("#AAAAAA")

      // ── Paints ───────────────────────────────────────────────────────────────
      private val bgPaint      = Paint(Paint.ANTI_ALIAS_FLAG)
      private val liquidPaint  = Paint(Paint.ANTI_ALIAS_FLAG)
      private val rimPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
      private val glassPaint   = Paint(Paint.ANTI_ALIAS_FLAG)
      private val crossPaint   = Paint(Paint.ANTI_ALIAS_FLAG)
      private val stitchPaint  = Paint(Paint.ANTI_ALIAS_FLAG)
      private val lcdBgPaint   = Paint(Paint.ANTI_ALIAS_FLAG)
      private val lcdTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
      private val lcdDimPaint  = Paint(Paint.ANTI_ALIAS_FLAG)
      private val btnPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
      private val iconPaint    = Paint(Paint.ANTI_ALIAS_FLAG)
      private val shadowPaint  = Paint(Paint.ANTI_ALIAS_FLAG)

      init {
          bgPaint.color = bgDark; bgPaint.style = Paint.Style.FILL
          rimPaint.style = Paint.Style.STROKE; rimPaint.strokeWidth = 3f
          glassPaint.style = Paint.Style.FILL
          crossPaint.color = Color.argb(140, 200, 255, 0); crossPaint.style = Paint.Style.STROKE; crossPaint.strokeWidth = 1.8f
          stitchPaint.color = stitchColor; stitchPaint.style = Paint.Style.STROKE; stitchPaint.strokeWidth = 1.5f
          stitchPaint.pathEffect = DashPathEffect(floatArrayOf(6f, 5f), 0f)
          lcdBgPaint.color = lcdBg; lcdBgPaint.style = Paint.Style.FILL
          lcdTextPaint.color = lcdText; lcdTextPaint.style = Paint.Style.FILL
          lcdTextPaint.typeface = Typeface.MONOSPACE; lcdTextPaint.textAlign = Paint.Align.CENTER
          lcdDimPaint.color = lcdDim; lcdDimPaint.style = Paint.Style.FILL
          lcdDimPaint.typeface = Typeface.MONOSPACE; lcdDimPaint.textAlign = Paint.Align.CENTER
          btnPaint.style = Paint.Style.FILL
          iconPaint.color = iconColor; iconPaint.style = Paint.Style.STROKE
          iconPaint.strokeWidth = 3f; iconPaint.strokeCap = Paint.Cap.ROUND
          shadowPaint.color = Color.argb(180, 0, 0, 0); shadowPaint.style = Paint.Style.FILL
          liquidPaint.style = Paint.Style.FILL
      }

      // ──────────────────────────────────────────────────────────────────────────
      override fun onDraw(canvas: Canvas) {
          super.onDraw(canvas)
          val w = width.toFloat(); val h = height.toFloat()

          // Leather background
          canvas.drawRect(0f, 0f, w, h, bgPaint)
          drawLeatherTexture(canvas, w, h)

          val pad = w * 0.04f

          // ── 1. HORIZONTAL TUBE (top) ─────────────────────────────────────────
          val hTubeTop  = h * 0.03f
          val hTubeBot  = h * 0.18f
          val hTubeL    = pad
          val hTubeR    = w - pad
          drawHorizTube(canvas, hTubeL, hTubeTop, hTubeR, hTubeBot, roll)

          // ── 2. VERTICAL TUBE (left) ──────────────────────────────────────────
          val vTubeL   = pad
          val vTubeR   = w * 0.22f
          val vTubeTop2 = h * 0.205f
          val vTubeBot2 = h * 0.685f
          drawVertTube(canvas, vTubeL, vTubeTop2, vTubeR, vTubeBot2, pitch)

          // ── 3. CIRCULAR VIAL (center-right) ──────────────────────────────────
          val circCx = (vTubeR + 20f + (w - pad)) / 2f
          val circCy = (vTubeTop2 + vTubeBot2) / 2f
          val circR  = minOf(
              (w - pad - vTubeR - 24f) / 2f,
              (vTubeBot2 - vTubeTop2) / 2f
          ) * 0.92f
          drawCircularVial(canvas, circCx, circCy, circR, pitch, roll)

          // ── 4. LCD READOUT ────────────────────────────────────────────────────
          val lcdTop = h * 0.71f
          val lcdBot = h * 0.785f
          drawLcdDisplay(canvas, pad, lcdTop, w - pad, lcdBot, roll, pitch)

          // ── 5. BUTTONS ────────────────────────────────────────────────────────
          val btnTop = h * 0.855f
          val btnBot = h * 0.96f
          drawButtons(canvas, pad, btnTop, w - pad, btnBot)
      }

      // ── Leather texture ───────────────────────────────────────────────────────
      private fun drawLeatherTexture(canvas: Canvas, w: Float, h: Float) {
          val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
              color = Color.argb(18, 255, 255, 255)
              style = Paint.Style.FILL
          }
          var y = 0f
          while (y < h) {
              var x = if ((y / 18f).toInt() % 2 == 0) 0f else 9f
              while (x < w) {
                  canvas.drawCircle(x, y, 3.5f, p)
                  x += 18f
              }
              y += 18f
          }
      }

      // ── Horizontal tube ───────────────────────────────────────────────────────
      private fun drawHorizTube(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, roll: Float) {
          val cr = (b - t) / 2f

          // Outer casing
          val casingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
              style = Paint.Style.FILL
              color = rimDark
          }
          canvas.drawRoundRect(RectF(l - 6f, t - 6f, r + 6f, b + 6f), cr + 6f, cr + 6f, casingPaint)
          rimPaint.color = rimLight; rimPaint.strokeWidth = 2f
          canvas.drawRoundRect(RectF(l - 6f, t - 6f, r + 6f, b + 6f), cr + 6f, cr + 6f, rimPaint)

          // Liquid fill — gradient green
          val lg = LinearGradient(0f, t, 0f, b,
              intArrayOf(limeGlass, limeA, limeB, limeA),
              floatArrayOf(0f, 0.3f, 0.7f, 1f), Shader.TileMode.CLAMP)
          liquidPaint.shader = lg
          canvas.drawRoundRect(RectF(l, t, r, b), cr, cr, liquidPaint)
          liquidPaint.shader = null

          // Stitched border inside
          canvas.drawRoundRect(RectF(l + 4f, t + 4f, r - 4f, b - 4f), cr - 3f, cr - 3f, stitchPaint)

          // Center vertical tick
          val cx = (l + r) / 2f; val cy = (t + b) / 2f
          canvas.drawLine(cx, t + 4f, cx, b - 4f, crossPaint)
          // Horizontal guide line
          canvas.drawLine(l + 8f, cy, r - 8f, cy, crossPaint)

          // Bubble
          val maxTravel = (r - l) * 0.38f
          val bx = cx + (-roll / 45f * maxTravel).coerceIn(-maxTravel, maxTravel)
          drawBubble(canvas, bx, cy, (b - t) * 0.30f, (b - t) * 0.40f)

          // Glass sheen top half
          glassPaint.shader = LinearGradient(0f, t, 0f, cy,
              intArrayOf(Color.argb(120, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l + 2f, t + 2f, r - 2f, cy), cr - 1f, cr - 1f, glassPaint)
          glassPaint.shader = null
      }

      // ── Vertical tube ─────────────────────────────────────────────────────────
      private fun drawVertTube(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, pitch: Float) {
          val cr = (r - l) / 2f

          // Outer casing
          val casingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = rimDark }
          canvas.drawRoundRect(RectF(l - 6f, t - 6f, r + 6f, b + 6f), cr + 6f, cr + 6f, casingPaint)
          rimPaint.color = rimLight; rimPaint.strokeWidth = 2f
          canvas.drawRoundRect(RectF(l - 6f, t - 6f, r + 6f, b + 6f), cr + 6f, cr + 6f, rimPaint)

          // Liquid
          val lg = LinearGradient(l, 0f, r, 0f,
              intArrayOf(limeGlass, limeA, limeB, limeA),
              floatArrayOf(0f, 0.3f, 0.7f, 1f), Shader.TileMode.CLAMP)
          liquidPaint.shader = lg
          canvas.drawRoundRect(RectF(l, t, r, b), cr, cr, liquidPaint)
          liquidPaint.shader = null

          // Stitched border
          canvas.drawRoundRect(RectF(l + 4f, t + 4f, r - 4f, b - 4f), cr - 3f, cr - 3f, stitchPaint)

          // Crosshairs
          val cx = (l + r) / 2f; val cy = (t + b) / 2f
          canvas.drawLine(cx, t + 8f, cx, b - 8f, crossPaint)
          canvas.drawLine(l + 4f, cy, r - 4f, cy, crossPaint)

          // Tick marks (every ~15% of height)
          val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
              color = Color.argb(100, 200, 255, 0); style = Paint.Style.STROKE; strokeWidth = 1.5f
          }
          for (i in -2..2) {
              val ty = cy + i * (b - t) * 0.15f
              if (ty > t + 8f && ty < b - 8f)
                  canvas.drawLine(l + 6f, ty, r - 6f, ty, tickPaint)
          }

          // Bubble (moves vertically with pitch)
          val maxTravel = (b - t) * 0.38f
          val by = cy + (pitch / 45f * maxTravel).coerceIn(-maxTravel, maxTravel)
          drawBubble(canvas, cx, by, (r - l) * 0.28f, (r - l) * 0.40f)

          // Glass sheen left half
          glassPaint.shader = LinearGradient(l, 0f, cx, 0f,
              intArrayOf(Color.argb(120, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l + 2f, t + 2f, cx, b - 2f), cr - 1f, cr - 1f, glassPaint)
          glassPaint.shader = null
      }

      // ── Circular vial ─────────────────────────────────────────────────────────
      private fun drawCircularVial(canvas: Canvas, cx: Float, cy: Float, r: Float, pitch: Float, roll: Float) {
          // Metallic outer rim (thick dark ring)
          for (i in 5 downTo 0) {
              val rimP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                  style = Paint.Style.STROKE
                  strokeWidth = (i * 3f) + 1f
                  color = when (i) {
                      5 -> Color.parseColor("#111111")
                      4 -> Color.parseColor("#333333")
                      3 -> Color.parseColor("#555555")
                      2 -> Color.parseColor("#3A3A3A")
                      1 -> Color.parseColor("#222222")
                      else -> Color.parseColor("#666666")
                  }
              }
              canvas.drawCircle(cx, cy, r + 10f + i * 3f, rimP)
          }

          // Green liquid fill
          val liquidGrad = RadialGradient(cx - r * 0.2f, cy - r * 0.2f, r * 1.3f,
              intArrayOf(limeGlass, limeA, limeB, limeDeep),
              floatArrayOf(0f, 0.4f, 0.75f, 1f), Shader.TileMode.CLAMP)
          liquidPaint.shader = liquidGrad
          canvas.drawCircle(cx, cy, r, liquidPaint)
          liquidPaint.shader = null

          // Stitched border ring
          canvas.drawCircle(cx, cy, r - 5f, stitchPaint)

          // Crosshair lines
          canvas.drawLine(cx - r + 6f, cy, cx + r - 6f, cy, crossPaint)
          canvas.drawLine(cx, cy - r + 6f, cx, cy + r - 6f, crossPaint)

          // Inner concentric circles (scope rings)
          val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
              style = Paint.Style.STROKE; strokeWidth = 1.5f
              color = Color.argb(130, 200, 255, 50)
          }
          canvas.drawCircle(cx, cy, r * 0.35f, ringPaint)
          canvas.drawCircle(cx, cy, r * 0.65f, ringPaint)

          // Bubble position (2D — both pitch and roll)
          val maxOff = r * 0.70f
          val rawX = (-roll / 45f) * maxOff
          val rawY = (-pitch / 45f) * maxOff
          val dist = sqrt(rawX * rawX + rawY * rawY)
          val bx: Float; val by: Float
          if (dist > maxOff * 0.9f) {
              bx = cx + rawX * (maxOff * 0.9f) / dist
              by = cy + rawY * (maxOff * 0.9f) / dist
          } else { bx = cx + rawX; by = cy + rawY }
          drawBubble(canvas, bx, by, r * 0.18f, r * 0.22f)

          // Glass sheen — upper-left arc
          glassPaint.shader = RadialGradient(cx - r * 0.35f, cy - r * 0.35f, r * 0.8f,
              intArrayOf(Color.argb(110, 255, 255, 255), Color.TRANSPARENT),
              null, Shader.TileMode.CLAMP)
          canvas.drawCircle(cx, cy, r, glassPaint)
          glassPaint.shader = null
      }

      // ── Bubble (realistic oval) ───────────────────────────────────────────────
      private fun drawBubble(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float) {
          // Drop shadow
          shadowPaint.color = Color.argb(80, 0, 0, 0)
          canvas.drawOval(RectF(cx - rx + 3f, cy - ry + 4f, cx + rx + 3f, cy + ry + 4f), shadowPaint)

          // Bubble body gradient
          val bGrad = RadialGradient(cx - rx * 0.3f, cy - ry * 0.3f, rx * 1.5f,
              intArrayOf(Color.argb(220, 220, 255, 100), Color.argb(180, 160, 220, 0), Color.argb(60, 80, 130, 0)),
              floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
          val bPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; shader = bGrad }
          canvas.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), bPaint)

          // Rim highlight
          val rimP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
              style = Paint.Style.STROKE; strokeWidth = 1.5f
              color = Color.argb(180, 200, 255, 80)
          }
          canvas.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), rimP)

          // White specular highlight (top-left)
          val specGrad = RadialGradient(cx - rx * 0.3f, cy - ry * 0.5f, rx * 0.4f,
              intArrayOf(Color.argb(200, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          val specP = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; shader = specGrad }
          canvas.drawOval(RectF(cx - rx * 0.65f, cy - ry * 0.85f, cx + rx * 0.1f, cy - ry * 0.1f), specP)
      }

      // ── LCD display ───────────────────────────────────────────────────────────
      private fun drawLcdDisplay(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, roll: Float, pitch: Float) {
          val cr = (b - t) / 2f

          // Casing
          val casingP = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = rimDark }
          canvas.drawRoundRect(RectF(l - 4f, t - 4f, r + 4f, b + 4f), cr + 4f, cr + 4f, casingP)
          rimPaint.color = rimGray; rimPaint.strokeWidth = 2f
          canvas.drawRoundRect(RectF(l - 4f, t - 4f, r + 4f, b + 4f), cr + 4f, cr + 4f, rimPaint)

          // LCD background
          canvas.drawRoundRect(RectF(l, t, r, b), cr, cr, lcdBgPaint)

          // Dim ghost digits background
          lcdDimPaint.textSize = (b - t) * 0.58f
          canvas.drawText("X: -88.8°  |  Y: -88.8°", (l + r) / 2f, b - (b - t) * 0.22f, lcdDimPaint)

          // Real values
          lcdTextPaint.textSize = (b - t) * 0.58f
          val xStr = "X: %+.1f°".format(-roll)
          val yStr = "Y: %+.1f°".format(pitch)
          val divider = "  |  "
          val cx = (l + r) / 2f
          canvas.drawText("$xStr$divider$yStr", cx, b - (b - t) * 0.22f, lcdTextPaint)

          // Glass sheen
          glassPaint.shader = LinearGradient(0f, t, 0f, (t + b) / 2f,
              intArrayOf(Color.argb(50, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l, t, r, (t + b) / 2f), cr, cr, glassPaint)
          glassPaint.shader = null
      }

      // ── Bottom buttons ────────────────────────────────────────────────────────
      private fun drawButtons(canvas: Canvas, l: Float, t: Float, r: Float, b: Float) {
          val totalW = r - l
          val btnSize = minOf((b - t), totalW / 4f - 12f)
          val spacing = (totalW - btnSize * 4f) / 5f
          val btnY = (t + b - btnSize) / 2f

          val rects = listOf(btnCalibrate, btnSound, btnLock, btnSettings)
          for (i in 0..3) {
              val bx = l + spacing + i * (btnSize + spacing)
              rects[i].set(bx, btnY, bx + btnSize, btnY + btnSize)
          }

          // Draw each button
          val cr = 12f
          for ((i, rect) in rects.withIndex()) {
              // Button shadow
              shadowPaint.color = Color.argb(120, 0, 0, 0)
              canvas.drawRoundRect(RectF(rect.left + 3f, rect.top + 3f, rect.right + 3f, rect.bottom + 3f), cr, cr, shadowPaint)

              // Button body gradient
              val bGrad = LinearGradient(0f, rect.top, 0f, rect.bottom,
                  intArrayOf(Color.parseColor("#333333"), Color.parseColor("#1A1A1A")),
                  null, Shader.TileMode.CLAMP)
              btnPaint.shader = bGrad
              canvas.drawRoundRect(rect, cr, cr, btnPaint)
              btnPaint.shader = null

              rimPaint.color = rimGray; rimPaint.strokeWidth = 1.5f
              canvas.drawRoundRect(rect, cr, cr, rimPaint)

              // Icon
              val ic = rect.centerX(); val jc = rect.centerY(); val is2 = btnSize * 0.28f
              iconPaint.strokeWidth = btnSize * 0.065f; iconPaint.strokeCap = Paint.Cap.ROUND
              when (i) {
                  0 -> drawCalibrateIcon(canvas, ic, jc, is2)
                  1 -> drawSoundIcon(canvas, ic, jc, is2)
                  2 -> drawLockIcon(canvas, ic, jc, is2)
                  3 -> drawSettingsIcon(canvas, ic, jc, is2)
              }
          }
      }

      private fun drawCalibrateIcon(canvas: Canvas, cx: Float, cy: Float, s: Float) {
          // Crosshair / target
          canvas.drawCircle(cx, cy, s, iconPaint)
          canvas.drawCircle(cx, cy, s * 0.4f, iconPaint)
          canvas.drawLine(cx - s * 1.4f, cy, cx - s * 1.1f, cy, iconPaint)
          canvas.drawLine(cx + s * 1.1f, cy, cx + s * 1.4f, cy, iconPaint)
          canvas.drawLine(cx, cy - s * 1.4f, cx, cy - s * 1.1f, iconPaint)
          canvas.drawLine(cx, cy + s * 1.1f, cx, cy + s * 1.4f, iconPaint)
      }

      private fun drawSoundIcon(canvas: Canvas, cx: Float, cy: Float, s: Float) {
          // Speaker body
          val path = Path()
          path.moveTo(cx - s * 0.9f, cy - s * 0.4f)
          path.lineTo(cx - s * 0.3f, cy - s * 0.4f)
          path.lineTo(cx + s * 0.2f, cy - s * 0.9f)
          path.lineTo(cx + s * 0.2f, cy + s * 0.9f)
          path.lineTo(cx - s * 0.3f, cy + s * 0.4f)
          path.lineTo(cx - s * 0.9f, cy + s * 0.4f)
          path.close()
          iconPaint.style = Paint.Style.STROKE
          canvas.drawPath(path, iconPaint)
          // Sound waves
          if (isSoundEnabled) {
              for (arc in listOf(s * 0.6f, s * 1.0f)) {
                  canvas.drawArc(RectF(cx + s * 0.1f, cy - arc, cx + s * 0.1f + arc * 2f, cy + arc), -60f, 120f, false, iconPaint)
              }
          }
      }

      private fun drawLockIcon(canvas: Canvas, cx: Float, cy: Float, s: Float) {
          // Lock body
          val bodyRect = RectF(cx - s * 0.7f, cy - s * 0.1f, cx + s * 0.7f, cy + s * 1.0f)
          canvas.drawRoundRect(bodyRect, s * 0.2f, s * 0.2f, iconPaint)
          // Shackle arc (open if not locked)
          if (isLocked) {
              canvas.drawArc(RectF(cx - s * 0.5f, cy - s * 1.0f, cx + s * 0.5f, cy + s * 0.1f), 180f, 180f, false, iconPaint)
          } else {
              canvas.drawArc(RectF(cx - s * 0.1f, cy - s * 1.0f, cx + s * 0.9f, cy + s * 0.1f), 180f, 180f, false, iconPaint)
          }
          // Keyhole dot
          val kp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = iconColor; style = Paint.Style.FILL }
          canvas.drawCircle(cx, cy + s * 0.4f, s * 0.18f, kp)
      }

      private fun drawSettingsIcon(canvas: Canvas, cx: Float, cy: Float, s: Float) {
          // Gear — circle center + teeth
          canvas.drawCircle(cx, cy, s * 0.45f, iconPaint)
          canvas.drawCircle(cx, cy, s * 0.18f, iconPaint)
          // 8 teeth
          for (i in 0..7) {
              val angle = Math.toRadians(i * 45.0)
              val innerR = s * 0.6f; val outerR = s * 1.0f
              val x1 = cx + cos(angle).toFloat() * innerR
              val y1 = cy + sin(angle).toFloat() * innerR
              val x2 = cx + cos(angle).toFloat() * outerR
              val y2 = cy + sin(angle).toFloat() * outerR
              canvas.drawLine(x1, y1, x2, y2, iconPaint)
          }
      }

      // ── Touch handling ────────────────────────────────────────────────────────
      override fun onTouchEvent(event: MotionEvent): Boolean {
          if (event.action == MotionEvent.ACTION_UP) {
              val x = event.x; val y = event.y
              when {
                  btnCalibrate.contains(x, y) -> { onCalibrateClick?.invoke(); return true }
                  btnSound.contains(x, y)     -> { onSoundClick?.invoke(); return true }
                  btnLock.contains(x, y)      -> { onLockClick?.invoke(); return true }
                  btnSettings.contains(x, y)  -> { onSettingsClick?.invoke(); return true }
              }
          }
          return super.onTouchEvent(event)
      }

      fun updateAngles(pitch: Float, roll: Float) {
          this.pitch = pitch
          this.roll  = roll
          invalidate()
      }
  }
  