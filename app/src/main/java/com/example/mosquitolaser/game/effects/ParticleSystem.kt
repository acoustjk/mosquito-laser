package com.example.mosquitolaser.game.effects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import kotlin.math.abs
import kotlin.random.Random

/**
 * Particle system for mosquito death explosions and laser sparks.
 */
class ParticleSystem {

    private val particles = mutableListOf<Particle>()

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,        // 0.0 to 1.0 (1 = full life)
        var maxLife: Float,
        var size: Float,
        var color: Int,
        var type: ParticleType
    )

    enum class ParticleType { SPARK, SMOKE, MOSQUITO_PART }

    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }

    private val smokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun emitMosquitoDeath(normX: Float, normY: Float, canvasW: Float, canvasH: Float) {
        val px = normX * canvasW
        val py = normY * canvasH

        // Sparks
        repeat(20) {
            val angle = Random.nextFloat() * 360f
            val speed = Random.nextFloat() * 8f + 2f
            val angleRad = Math.toRadians(angle.toDouble())
            particles.add(
                Particle(
                    x = px, y = py,
                    vx = (Math.cos(angleRad) * speed).toFloat(),
                    vy = (Math.sin(angleRad) * speed).toFloat(),
                    life = 1f,
                    maxLife = 1f,
                    size = Random.nextFloat() * 4f + 2f,
                    color = if (Random.nextBoolean()) Color.YELLOW else Color.RED,
                    type = ParticleType.SPARK
                )
            )
        }

        // Smoke
        repeat(8) {
            val angle = Random.nextFloat() * 360f
            val speed = Random.nextFloat() * 2f + 0.5f
            val angleRad = Math.toRadians(angle.toDouble())
            particles.add(
                Particle(
                    x = px, y = py,
                    vx = (Math.cos(angleRad) * speed).toFloat(),
                    vy = (Math.sin(angleRad) * speed).toFloat() - 1f,
                    life = 1f,
                    maxLife = 0.7f,
                    size = Random.nextFloat() * 10f + 5f,
                    color = Color.argb(150, 100, 100, 100),
                    type = ParticleType.SMOKE
                )
            )
        }
    }

    fun emitLaserSpark(normX: Float, normY: Float, canvasW: Float, canvasH: Float, laserColor: Int) {
        val px = normX * canvasW
        val py = normY * canvasH

        repeat(5) {
            val angle = Random.nextFloat() * 360f
            val speed = Random.nextFloat() * 4f + 1f
            val angleRad = Math.toRadians(angle.toDouble())
            particles.add(
                Particle(
                    x = px, y = py,
                    vx = (Math.cos(angleRad) * speed).toFloat(),
                    vy = (Math.sin(angleRad) * speed).toFloat(),
                    life = 1f,
                    maxLife = 0.3f,
                    size = Random.nextFloat() * 3f + 1f,
                    color = laserColor,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    fun update(deltaMs: Long) {
        val dt = deltaMs / 16f // normalized to 60fps
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 0.2f * dt // gravity for sparks

            // Decay
            val decayRate = when (p.type) {
                ParticleType.SPARK -> 0.03f
                ParticleType.SMOKE -> 0.015f
                ParticleType.MOSQUITO_PART -> 0.02f
            }
            p.life -= decayRate * dt
            if (p.life <= 0f) iterator.remove()
        }
    }

    fun draw(canvas: Canvas) {
        for (p in particles) {
            val alpha = (p.life * 255f).toInt().coerceIn(0, 255)
            when (p.type) {
                ParticleType.SPARK -> {
                    sparkPaint.color = p.color
                    sparkPaint.alpha = alpha
                    canvas.drawCircle(p.x, p.y, p.size * p.life, sparkPaint)
                }
                ParticleType.SMOKE, ParticleType.MOSQUITO_PART -> {
                    smokePaint.color = p.color
                    smokePaint.alpha = alpha / 2
                    val growSize = p.size * (2f - p.life)
                    canvas.drawCircle(p.x, p.y, growSize, smokePaint)
                }
            }
        }
    }

    fun hasActiveParticles() = particles.isNotEmpty()
}
