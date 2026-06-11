package com.example.mosquitolaser

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.View
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

/**
 * Animated background view for the main menu.
 * Shows floating particles and pulsing laser beams.
 */
class MenuBackgroundView(context: Context) : View(context) {

    private var w = 0f
    private var h = 0f

    private data class FloatingParticle(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        val size: Float, val color: Int
    )

    private val particles = (0..40).map {
        FloatingParticle(
            x = Random.nextFloat(),
            y = Random.nextFloat(),
            vx = (Random.nextFloat() - 0.5f) * 0.0003f,
            vy = (Random.nextFloat() - 0.5f) * 0.0003f,
            size = Random.nextFloat() * 3f + 1f,
            color = listOf(
                Color.argb(150, 0, 200, 255),
                Color.argb(100, 100, 255, 200),
                Color.argb(80, 255, 100, 200)
            ).random()
        )
    }.toMutableList()

    private val bgPaint = Paint()
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val laserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    }

    // Laser beams in background
    private val bgLasers = (0..3).map {
        floatArrayOf(
            Random.nextFloat(), Random.nextFloat(),
            Random.nextFloat(), Random.nextFloat()
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.w = w.toFloat()
        this.h = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = SystemClock.elapsedRealtime()

        // Gradient background
        val gradient = LinearGradient(0f, 0f, 0f, h,
            Color.parseColor("#03030F"),
            Color.parseColor("#080815"),
            Shader.TileMode.CLAMP
        )
        bgPaint.shader = gradient
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Pulsing center glow
        val pulse = (1f + sin(now * 0.001f)) * 0.5f
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(w / 2f, h * 0.4f, w * 0.5f,
                Color.argb((40 + pulse * 30).toInt(), 0, 150, 255),
                Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, glowPaint)

        // Background laser lines (slowly moving)
        for ((i, laser) in bgLasers.withIndex()) {
            val phase = now * 0.0002f + i * 1.5f
            val alpha = (80 + sin(phase) * 50).toInt().coerceIn(10, 130)
            laserPaint.color = Color.argb(alpha, 0, 180, 255)
            val x1 = laser[0] * w
            val y1 = laser[1] * h
            val x2 = (laser[0] + cos(phase.toDouble()) * 0.4f).toFloat().coerceIn(0f, 1f) * w
            val y2 = (laser[1] + sin(phase.toDouble()) * 0.4f).toFloat().coerceIn(0f, 1f) * h
            canvas.drawLine(x1, y1, x2, y2, laserPaint)
        }

        // Floating particles
        for (p in particles) {
            p.x = (p.x + p.vx + 1f) % 1f
            p.y = (p.y + p.vy + 1f) % 1f
            particlePaint.color = p.color
            canvas.drawCircle(p.x * w, p.y * h, p.size, particlePaint)
        }

        // Grid lines (subtle)
        val gridPaint = Paint().apply {
            color = Color.argb(20, 0, 150, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val cols = 8
        val rows = 14
        for (i in 0..cols) {
            canvas.drawLine(w * i / cols, 0f, w * i / cols, h, gridPaint)
        }
        for (i in 0..rows) {
            canvas.drawLine(0f, h * i / rows, w, h * i / rows, gridPaint)
        }

        invalidate()
    }
}
