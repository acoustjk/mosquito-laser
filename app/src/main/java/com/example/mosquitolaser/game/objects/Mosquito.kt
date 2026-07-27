package com.example.mosquitolaser.game.objects

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents a mosquito target on the game grid.
 * Position is in normalized (0-1) coordinates.
 */
class Mosquito(
    val id: Int,
    var x: Float,
    var y: Float,
    val movementType: MovementType,
    val moveSpeed: Float = 0.0f,
    val moveRange: Float = 0.1f,
    val movePhase: Float = 0.0f,
    var isAlive: Boolean = true,
    val hitRadius: Float = 0.04f
) {
    /**
     * Describes how a mosquito moves across the game grid.
     * Declared as a direct nested class (not in companion object) so
     * it can be referenced as Mosquito.MovementType from anywhere.
     */
    enum class MovementType {
        STATIC,
        LINEAR,
        CIRCULAR,
        RANDOM
    }

    val startX: Float = x
    val startY: Float = y

    // 3-second laser burn mechanics
    var burnTimeMs: Long = 0L
    val requiredBurnTimeMs: Long = 3_000L
    val burnProgress: Float get() = (burnTimeMs.toFloat() / requiredBurnTimeMs).coerceIn(0f, 1f)
    var isBeingHitByLaser: Boolean = false

    private var timeMs: Long = 0L

    // Random-movement state
    private var randomTargetX: Float = x
    private var randomTargetY: Float = y
    private var timeSinceTargetChangeMs: Long = 0L
    private val randomTargetIntervalMs: Long = 3_000L
    private val randomMoveSpeed: Float = 0.00015f

    fun update(deltaMs: Long) {
        if (!isAlive) return
        timeMs += deltaMs

        when (movementType) {
            MovementType.STATIC -> { /* no movement */ }

            MovementType.LINEAR -> {
                val t = timeMs / 1000f
                x = startX + sin(t * moveSpeed + movePhase) * moveRange
            }

            MovementType.CIRCULAR -> {
                val t = timeMs / 1000f
                x = startX + cos(t * moveSpeed + movePhase) * moveRange
                y = startY + sin(t * moveSpeed + movePhase) * moveRange
            }

            MovementType.RANDOM -> {
                updateRandom(deltaMs)
            }
        }
    }

    fun checkHit(laserX: Float, laserY: Float): Boolean {
        if (!isAlive) return false
        val dx = laserX - x
        val dy = laserY - y
        return sqrt(dx * dx + dy * dy) < hitRadius
    }

    private fun updateRandom(deltaMs: Long) {
        timeSinceTargetChangeMs += deltaMs
        if (timeSinceTargetChangeMs >= randomTargetIntervalMs) {
            val angle = Math.random() * 2.0 * Math.PI
            val radius = Math.random().toFloat() * moveRange
            randomTargetX = (startX + cos(angle).toFloat() * radius).coerceIn(0f, 1f)
            randomTargetY = (startY + sin(angle).toFloat() * radius).coerceIn(0f, 1f)
            timeSinceTargetChangeMs = 0L
        }
        val dx = randomTargetX - x
        val dy = randomTargetY - y
        val dist = sqrt(dx * dx + dy * dy)
        val step = randomMoveSpeed * deltaMs
        if (dist > step) {
            x += (dx / dist) * step
            y += (dy / dist) * step
        } else {
            x = randomTargetX
            y = randomTargetY
        }
    }
}
