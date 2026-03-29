package com.inclinometer.app.ui.bubble

  import android.content.Context
  import android.graphics.*
  import android.util.AttributeSet
  import android.view.MotionEvent
  import android.view.View
  import kotlin.math.*

  class BubbleLevelView @JvmOverloads constructor(
      context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
  ) : View(context, attrs, defStyle) {

      var pitch = 0f
      var roll  = 0f
      var isSoundEnabled = true
      var isLocked       = false

      var onCalibrateClick: (() -> Unit)? = null
      var onSoundClick:     (() -> Unit)? = null
      var onLockClick:      (() -> Unit)? = null
      var onSettingsClick:  (() -> Unit)? = null

      private val btnRects = Array(4) { RectF() }

      // ─── Palette (from reference image) ───────────────────────────────────────
      private val C_BG        = Color.parseColor("#0E0E0E")
      private val C_LIQUID_1  = Color.parseColor("#C8E600")   // top bright
      private val C_LIQUID_2  = Color.parseColor("#8DBF00")   // mid
      private val C_LIQUID_3  = Color.parseColor("#5A8200")   // bottom deep
      private val C_LIQUID_4  = Color.parseColor("#A0D400")   // lower highlight
      private val C_TUBE_CASE = Color.parseColor("#1C1C1C")
      private val C_TUBE_RIM  = Color.parseColor("#3E3E3E")
      private val C_BUBBLE_1  = Color.parseColor("#EEFF88")
      private val C_BUBBLE_2  = Color.parseColor("#A8D400")
      private val C_CROSS     = Color.parseColor("#6E9900")
      private val C_STITCH    = Color.parseColor("#4A6600")
      private val C_LCD_BG    = Color.parseColor("#090F00")
      private val C_LCD_TXT   = Color.parseColor("#7AE800")
      private val C_LCD_DIM   = Color.parseColor("#1A2A00")
      private val C_BTN_TOP   = Color.parseColor("#2E2E2E")
      private val C_BTN_BOT   = Color.parseColor("#181818")
      private val C_ICON      = Color.parseColor("#999999")
      private val C_BEZEL_1   = Color.parseColor("#484848")
      private val C_BEZEL_2   = Color.parseColor("#181818")
      private val C_BEZEL_3   = Color.parseColor("#5A5A5A")

      // ─── Reusable paint objects ────────────────────────────────────────────────
      private val pFill   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
      private val pStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
      private val pText   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.FILL
          typeface = Typeface.MONOSPACE
          textAlign = Paint.Align.CENTER
      }
      private val pStitch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = 1.5f
          color = C_STITCH
          pathEffect = DashPathEffect(floatArrayOf(5f, 4f), 0f)
      }

      // ─── Draw ──────────────────────────────────────────────────────────────────
      override fun onDraw(canvas: Canvas) {
          val W = width.toFloat()
          val H = height.toFloat()
          // Background
          pFill.color = C_BG
          canvas.drawRect(0f, 0f, W, H, pFill)
          drawLeather(canvas, W, H)

          val pad = W * 0.035f

          // 1 ── Horizontal tube
          drawHTube(canvas,
              l = pad,   t = H * 0.030f,
              r = W-pad, b = H * 0.185f,
              roll = roll)

          // 2 ── Vertical tube
          drawVTube(canvas,
              l = pad,       t = H * 0.210f,
              r = W * 0.235f, b = H * 0.695f,
              pitch = pitch)

          // 3 ── Circular vial
          val vialCx = W * 0.630f
          val vialCy = H * 0.455f
          val vialR  = minOf(W * 0.305f, H * 0.250f)
          drawCircle(canvas, vialCx, vialCy, vialR, pitch, roll)

          // 4 ── LCD readout
          drawLCD(canvas,
              l = pad,   t = H * 0.730f,
              r = W-pad, b = H * 0.800f,
              xDeg = -roll, yDeg = pitch)

          // 5 ── Buttons
          drawButtons(canvas,
              l = W * 0.055f, t = H * 0.860f,
              r = W * 0.945f, b = H * 0.955f)
      }

      // ─── Leather background ────────────────────────────────────────────────────
      private fun drawLeather(canvas: Canvas, W: Float, H: Float) {
          pFill.color = Color.argb(14, 255, 255, 255)
          val step = 20f
          var row = 0
          var y = 0f
          while (y < H) {
              val xOff = if (row % 2 == 0) 0f else step / 2f
              var x = xOff
              while (x < W) {
                  canvas.drawCircle(x, y, 3f, pFill)
                  x += step
              }
              y += step * 0.7f
              row++
          }
      }

      // ─── Horizontal tube ──────────────────────────────────────────────────────
      private fun drawHTube(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, roll: Float) {
          val cr = (b - t) / 2f
          val cx = (l + r) / 2f
          val cy = (t + b) / 2f
          val rect = RectF(l, t, r, b)
          val outerRect = RectF(l - 8f, t - 8f, r + 8f, b + 8f)
          val outerCr = cr + 8f

          // Outer casing shadow
          pFill.color = Color.argb(140, 0, 0, 0)
          canvas.drawRoundRect(RectF(outerRect.left+4, outerRect.top+4, outerRect.right+4, outerRect.bottom+4), outerCr, outerCr, pFill)

          // Outer metal casing
          pFill.shader = LinearGradient(0f, outerRect.top, 0f, outerRect.bottom,
              intArrayOf(C_BEZEL_1, C_BEZEL_2, C_BEZEL_3), floatArrayOf(0f,.5f,1f), Shader.TileMode.CLAMP)
          canvas.drawRoundRect(outerRect, outerCr, outerCr, pFill)
          pFill.shader = null

          // Liquid fill
          pFill.shader = LinearGradient(0f, t, 0f, b,
              intArrayOf(C_LIQUID_1, C_LIQUID_2, C_LIQUID_3, C_LIQUID_4),
              floatArrayOf(0f, 0.38f, 0.70f, 1f), Shader.TileMode.CLAMP)
          canvas.drawRoundRect(rect, cr, cr, pFill)
          pFill.shader = null

          // Stitched inner border
          pStitch.strokeWidth = 1.8f
          canvas.drawRoundRect(RectF(l+5f, t+5f, r-5f, b-5f), cr-4f, cr-4f, pStitch)

          // Center divider (vertical dashed line)
          pStroke.color = Color.argb(120, 80, 120, 0); pStroke.strokeWidth = 2f
          pStroke.pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
          canvas.drawLine(cx, t + cr * 0.3f, cx, b - cr * 0.3f, pStroke)
          pStroke.pathEffect = null

          // Horizontal center line
          pStroke.color = Color.argb(100, 80, 120, 0); pStroke.strokeWidth = 1.5f
          canvas.drawLine(l + cr, cy, r - cr, cy, pStroke)

          // Bubble
          val maxX = (r - l - cr * 4f) * 0.45f
          val bx = cx + (-roll / 45f * maxX).coerceIn(-maxX, maxX)
          drawOvalBubble(canvas, bx, cy, (b-t) * 0.31f, (b-t) * 0.42f)

          // Glass top sheen (white gradient — upper 40%)
          pFill.shader = LinearGradient(0f, t, 0f, t + (b - t) * 0.45f,
              intArrayOf(Color.argb(160, 255, 255, 255), Color.argb(40, 255, 255, 255), Color.TRANSPARENT),
              floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l+2f, t+2f, r-2f, t + (b-t)*0.48f), cr-1f, cr-1f, pFill)
          pFill.shader = null

          // Bottom deep shadow
          pFill.shader = LinearGradient(0f, b - (b-t)*0.25f, 0f, b,
              intArrayOf(Color.TRANSPARENT, Color.argb(80, 0, 0, 0)), null, Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l+2f, b-(b-t)*0.28f, r-2f, b-2f), cr-1f, cr-1f, pFill)
          pFill.shader = null
      }

      // ─── Vertical tube ────────────────────────────────────────────────────────
      private fun drawVTube(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, pitch: Float) {
          val cr = (r - l) / 2f
          val cx = (l + r) / 2f
          val cy = (t + b) / 2f
          val rect = RectF(l, t, r, b)
          val outerRect = RectF(l-8f, t-8f, r+8f, b+8f)
          val outerCr = cr + 8f

          // Shadow
          pFill.color = Color.argb(140, 0, 0, 0)
          canvas.drawRoundRect(RectF(outerRect.left+4, outerRect.top+4, outerRect.right+4, outerRect.bottom+4), outerCr, outerCr, pFill)

          // Metal casing
          pFill.shader = LinearGradient(l, 0f, r, 0f,
              intArrayOf(C_BEZEL_1, C_BEZEL_2, C_BEZEL_3), floatArrayOf(0f,.5f,1f), Shader.TileMode.CLAMP)
          canvas.drawRoundRect(outerRect, outerCr, outerCr, pFill)
          pFill.shader = null

          // Liquid fill
          pFill.shader = LinearGradient(l, 0f, r, 0f,
              intArrayOf(C_LIQUID_1, C_LIQUID_2, C_LIQUID_3, C_LIQUID_4),
              floatArrayOf(0f, 0.38f, 0.70f, 1f), Shader.TileMode.CLAMP)
          canvas.drawRoundRect(rect, cr, cr, pFill)
          pFill.shader = null

          // Stitched border
          canvas.drawRoundRect(RectF(l+5f, t+5f, r-5f, b-5f), cr-4f, cr-4f, pStitch)

          // Center horizontal dashed line
          pStroke.color = Color.argb(120, 80, 120, 0); pStroke.strokeWidth = 2f
          pStroke.pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
          canvas.drawLine(l+cr*0.3f, cy, r-cr*0.3f, cy, pStroke)
          pStroke.pathEffect = null

          // Vertical center line
          pStroke.color = Color.argb(100, 80, 120, 0); pStroke.strokeWidth = 1.5f
          canvas.drawLine(cx, t + cr, cx, b - cr, pStroke)

          // Tick marks
          pStroke.color = Color.argb(80, 90, 130, 0); pStroke.strokeWidth = 1.5f
          for (i in listOf(-0.25f, 0.25f)) {
              val ty = cy + i * (b - t)
              canvas.drawLine(l + cr * 0.5f, ty, r - cr * 0.5f, ty, pStroke)
          }

          // Bubble
          val maxY = (b - t - cr * 4f) * 0.44f
          val by = cy + (pitch / 45f * maxY).coerceIn(-maxY, maxY)
          drawOvalBubble(canvas, cx, by, (r-l) * 0.30f, (r-l) * 0.40f)

          // Glass left sheen
          pFill.shader = LinearGradient(l, 0f, l + (r-l)*0.5f, 0f,
              intArrayOf(Color.argb(160, 255, 255, 255), Color.argb(40, 255, 255, 255), Color.TRANSPARENT),
              floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l+2f, t+2f, l+(r-l)*0.52f, b-2f), cr-1f, cr-1f, pFill)
          pFill.shader = null
      }

      // ─── Circular vial ────────────────────────────────────────────────────────
      private fun drawCircle(canvas: Canvas, cx: Float, cy: Float, r: Float, pitch: Float, roll: Float) {
          // Drop shadow
          pFill.color = Color.argb(160, 0, 0, 0)
          canvas.drawCircle(cx + 5f, cy + 6f, r + 14f, pFill)

          // ── Multi-layer bezel (thick metallic ring) ──
          // Outer dark ring
          pFill.shader = SweepGradient(cx, cy,
              intArrayOf(C_BEZEL_1, C_BEZEL_3, C_BEZEL_2, C_BEZEL_3, C_BEZEL_1),
              floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f))
          canvas.drawCircle(cx, cy, r + 16f, pFill)
          pFill.shader = null

          // Bezel ring (gradient metallic)
          pFill.shader = RadialGradient(cx, cy, r+16f,
              intArrayOf(C_BEZEL_1, C_BEZEL_2, C_TUBE_CASE),
              floatArrayOf(0.75f, 0.88f, 1f), Shader.TileMode.CLAMP)
          canvas.drawCircle(cx, cy, r + 14f, pFill)
          pFill.shader = null

          // Inner rim highlight
          pStroke.color = C_BEZEL_3; pStroke.strokeWidth = 1.5f; pStroke.pathEffect = null
          canvas.drawCircle(cx, cy, r + 2f, pStroke)

          // Liquid fill
          pFill.shader = RadialGradient(cx - r*0.25f, cy - r*0.25f, r*1.3f,
              intArrayOf(C_LIQUID_1, C_LIQUID_2, C_LIQUID_3),
              floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
          canvas.drawCircle(cx, cy, r, pFill)
          pFill.shader = null

          // Stitched border inside vial
          pStitch.strokeWidth = 1.8f
          canvas.drawCircle(cx, cy, r - 5f, pStitch)

          // Concentric rings (scope circles)
          pStroke.color = Color.argb(90, 100, 160, 0); pStroke.strokeWidth = 1.5f
          canvas.drawCircle(cx, cy, r * 0.30f, pStroke)
          canvas.drawCircle(cx, cy, r * 0.62f, pStroke)

          // Crosshair lines
          pStroke.color = Color.argb(110, 100, 160, 0); pStroke.strokeWidth = 2f
          canvas.drawLine(cx - r + 8f, cy, cx + r - 8f, cy, pStroke)
          canvas.drawLine(cx, cy - r + 8f, cx, cy + r - 8f, pStroke)

          // Bubble (2D: pitch + roll combined)
          val maxOff = r * 0.68f
          var bx = cx + (-roll  / 45f) * maxOff
          var by = cy + (-pitch / 45f) * maxOff
          val dist = hypot(bx - cx, by - cy)
          if (dist > maxOff * 0.92f) {
              val ang = atan2(by - cy, bx - cx)
              bx = cx + cos(ang) * maxOff * 0.92f
              by = cy + sin(ang) * maxOff * 0.92f
          }
          drawOvalBubble(canvas, bx, by, r * 0.175f, r * 0.175f)

          // Glass sheen (upper-left radial highlight)
          pFill.shader = RadialGradient(cx - r*0.4f, cy - r*0.4f, r * 0.85f,
              intArrayOf(Color.argb(130, 255, 255, 255), Color.argb(30, 255, 255, 255), Color.TRANSPARENT),
              floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
          canvas.drawCircle(cx, cy, r, pFill)
          pFill.shader = null
      }

      // ─── Oval bubble ──────────────────────────────────────────────────────────
      private fun drawOvalBubble(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float) {
          val oval = RectF(cx - rx, cy - ry, cx + rx, cy + ry)

          // Shadow
          pFill.color = Color.argb(70, 0, 0, 0)
          canvas.drawOval(RectF(cx-rx+3f, cy-ry+4f, cx+rx+3f, cy+ry+4f), pFill)

          // Body
          pFill.shader = RadialGradient(cx - rx*0.25f, cy - ry*0.25f, rx * 1.4f,
              intArrayOf(Color.argb(230, 220, 255, 100), Color.argb(200, 155, 215, 0), Color.argb(80, 60, 100, 0)),
              floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
          canvas.drawOval(oval, pFill)
          pFill.shader = null

          // Outer rim
          pStroke.color = Color.argb(160, 180, 240, 60); pStroke.strokeWidth = 1.5f; pStroke.pathEffect = null
          canvas.drawOval(oval, pStroke)

          // White specular highlight (top-left)
          pFill.shader = RadialGradient(cx - rx*0.3f, cy - ry*0.5f, rx * 0.45f,
              intArrayOf(Color.argb(210, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          canvas.drawOval(RectF(cx-rx*0.75f, cy-ry*0.90f, cx+rx*0.15f, cy-ry*0.05f), pFill)
          pFill.shader = null

          // Small bottom reflection
          pFill.shader = RadialGradient(cx + rx*0.2f, cy + ry*0.7f, rx*0.25f,
              intArrayOf(Color.argb(80, 200, 255, 80), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          canvas.drawOval(oval, pFill)
          pFill.shader = null
      }

      // ─── LCD display ──────────────────────────────────────────────────────────
      private fun drawLCD(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, xDeg: Float, yDeg: Float) {
          val cr = (b - t) / 2f
          val outerRect = RectF(l - 6f, t - 6f, r + 6f, b + 6f)
          val outerCr = cr + 6f
          val cx = (l + r) / 2f
          val textY = b - (b - t) * 0.20f

          // Casing shadow
          pFill.color = Color.argb(130, 0, 0, 0)
          canvas.drawRoundRect(RectF(outerRect.left+4, outerRect.top+4, outerRect.right+4, outerRect.bottom+4), outerCr, outerCr, pFill)

          // Metal casing
          pFill.shader = LinearGradient(0f, outerRect.top, 0f, outerRect.bottom,
              intArrayOf(C_BEZEL_1, C_BEZEL_2, C_BEZEL_3), floatArrayOf(0f,.5f,1f), Shader.TileMode.CLAMP)
          canvas.drawRoundRect(outerRect, outerCr, outerCr, pFill)
          pFill.shader = null

          // LCD panel
          pFill.color = C_LCD_BG
          canvas.drawRoundRect(RectF(l, t, r, b), cr, cr, pFill)

          // Inner green glow
          pFill.shader = RadialGradient(cx, (t+b)/2f, (r-l)*0.4f,
              intArrayOf(Color.argb(30, 100, 200, 0), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l, t, r, b), cr, cr, pFill)
          pFill.shader = null

          val ts = (b - t) * 0.60f

          // Ghost/dim digits
          pText.color = C_LCD_DIM; pText.textSize = ts
          canvas.drawText("X: -88.8°  |  Y: -88.8°", cx, textY, pText)

          // Live values
          pText.color = C_LCD_TXT; pText.textSize = ts
          canvas.drawText("X: %+.1f°  |  Y: %+.1f°".format(xDeg, yDeg), cx, textY, pText)

          // Glass sheen
          pFill.shader = LinearGradient(0f, t, 0f, (t+b)/2f,
              intArrayOf(Color.argb(40, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
          canvas.drawRoundRect(RectF(l+2f, t+2f, r-2f, (t+b)/2f), cr-1f, cr-1f, pFill)
          pFill.shader = null
      }

      // ─── Bottom buttons ────────────────────────────────────────────────────────
      private fun drawButtons(canvas: Canvas, l: Float, t: Float, r: Float, b: Float) {
          val gap = (r - l) / 20f
          val size = (r - l - gap * 3f) / 4f
          for (i in 0..3) {
              val bx = l + i * (size + gap)
              btnRects[i].set(bx, t, bx + size, b)
          }
          val cr = 14f
          for ((i, rect) in btnRects.withIndex()) {
              // Shadow
              pFill.color = Color.argb(120, 0, 0, 0)
              canvas.drawRoundRect(RectF(rect.left+3f, rect.top+3f, rect.right+3f, rect.bottom+3f), cr, cr, pFill)
              // Gradient body
              pFill.shader = LinearGradient(0f, rect.top, 0f, rect.bottom,
                  intArrayOf(C_BTN_TOP, C_BTN_BOT), null, Shader.TileMode.CLAMP)
              canvas.drawRoundRect(rect, cr, cr, pFill)
              pFill.shader = null
              // Border
              pStroke.color = Color.parseColor("#3A3A3A"); pStroke.strokeWidth = 1.5f; pStroke.pathEffect = null
              canvas.drawRoundRect(rect, cr, cr, pStroke)
              // Top sheen
              pFill.shader = LinearGradient(0f, rect.top, 0f, rect.top + (rect.bottom-rect.top)*0.4f,
                  intArrayOf(Color.argb(50, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
              canvas.drawRoundRect(rect, cr, cr, pFill)
              pFill.shader = null
              // Icon
              val ic = rect.centerX(); val jc = rect.centerY(); val s = size * 0.26f
              pStroke.color = C_ICON; pStroke.strokeWidth = size * 0.064f; pStroke.strokeCap = Paint.Cap.ROUND
              pStroke.pathEffect = null
              when (i) {
                  0 -> drawIconCalibrate(canvas, ic, jc, s)
                  1 -> drawIconSound(canvas, ic, jc, s)
                  2 -> drawIconLock(canvas, ic, jc, s)
                  3 -> drawIconGear(canvas, ic, jc, s)
              }
          }
      }

      private fun drawIconCalibrate(c: Canvas, cx: Float, cy: Float, s: Float) {
          c.drawCircle(cx, cy, s, pStroke)
          c.drawCircle(cx, cy, s * 0.38f, pStroke)
          c.drawLine(cx - s*1.5f, cy, cx - s*1.08f, cy, pStroke)
          c.drawLine(cx + s*1.08f, cy, cx + s*1.5f, cy, pStroke)
          c.drawLine(cx, cy - s*1.5f, cx, cy - s*1.08f, pStroke)
          c.drawLine(cx, cy + s*1.08f, cx, cy + s*1.5f, pStroke)
      }

      private fun drawIconSound(c: Canvas, cx: Float, cy: Float, s: Float) {
          val path = Path().also { p ->
              p.moveTo(cx - s*0.9f, cy - s*0.45f)
              p.lineTo(cx - s*0.2f, cy - s*0.45f)
              p.lineTo(cx + s*0.35f, cy - s)
              p.lineTo(cx + s*0.35f, cy + s)
              p.lineTo(cx - s*0.2f, cy + s*0.45f)
              p.lineTo(cx - s*0.9f, cy + s*0.45f)
              p.close()
          }
          c.drawPath(path, pStroke)
          if (isSoundEnabled) {
              c.drawArc(RectF(cx+s*0.25f, cy-s*0.7f, cx+s*1.65f, cy+s*0.7f), -50f, 100f, false, pStroke)
              c.drawArc(RectF(cx+s*0.55f, cy-s*1.1f, cx+s*2.05f, cy+s*1.1f), -50f, 100f, false, pStroke)
          } else {
              c.drawLine(cx+s*0.4f, cy-s*0.8f, cx+s*1.4f, cy+s*0.8f, pStroke)
              c.drawLine(cx+s*1.4f, cy-s*0.8f, cx+s*0.4f, cy+s*0.8f, pStroke)
          }
      }

      private fun drawIconLock(c: Canvas, cx: Float, cy: Float, s: Float) {
          c.drawRoundRect(RectF(cx-s*0.75f, cy-s*0.05f, cx+s*0.75f, cy+s*1.05f), s*0.22f, s*0.22f, pStroke)
          if (isLocked)
              c.drawArc(RectF(cx-s*0.5f, cy-s*1.1f, cx+s*0.5f, cy+s*0.05f), 180f, 180f, false, pStroke)
          else
              c.drawArc(RectF(cx+s*0.0f, cy-s*1.1f, cx+s*1.0f, cy+s*0.05f), 180f, 180f, false, pStroke)
          pFill.color = C_ICON
          c.drawCircle(cx, cy + s*0.45f, s*0.18f, pFill)
      }

      private fun drawIconGear(c: Canvas, cx: Float, cy: Float, s: Float) {
          c.drawCircle(cx, cy, s*0.5f, pStroke)
          for (i in 0..7) {
              val a = Math.toRadians(i * 45.0)
              c.drawLine(
                  cx + cos(a).toFloat()*s*0.62f, cy + sin(a).toFloat()*s*0.62f,
                  cx + cos(a).toFloat()*s*1.05f, cy + sin(a).toFloat()*s*1.05f, pStroke)
          }
      }

      // ─── Touch ────────────────────────────────────────────────────────────────
      override fun onTouchEvent(ev: MotionEvent): Boolean {
          if (ev.action == MotionEvent.ACTION_UP) {
              val x = ev.x; val y = ev.y
              if (btnRects[0].contains(x, y)) { onCalibrateClick?.invoke(); return true }
              if (btnRects[1].contains(x, y)) { onSoundClick?.invoke();     return true }
              if (btnRects[2].contains(x, y)) { onLockClick?.invoke();      return true }
              if (btnRects[3].contains(x, y)) { onSettingsClick?.invoke();  return true }
          }
          return super.onTouchEvent(ev)
      }

      fun updateAngles(pitch: Float, roll: Float) {
          this.pitch = pitch; this.roll = roll; invalidate()
      }
  }
  