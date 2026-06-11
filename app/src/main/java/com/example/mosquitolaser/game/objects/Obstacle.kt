package com.example.mosquitolaser.game.objects

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

/**
 * Represents an obstacle on the game grid that can block or attenuate laser beams.
 */
class Obstacle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isSemiTransparent: Boolean = false
) {
    private val right: Float  get() = x + width
    private val bottom: Float get() = y + height

    /**
     * Tests whether the line segment from (x1, y1) to (x2, y2) intersects
     * this obstacle using the Liang-Barsky algorithm.
     */
    fun intersectsLaser(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        val dx = x2 - x1
        val dy = y2 - y1

        var tMin = 0f
        var tMax = 1f

        // Left boundary
        val r1 = clipSlab(-dx, x1 - x, tMin, tMax) ?: return false
        tMin = r1.first; tMax = r1.second

        // Right boundary
        val r2 = clipSlab(dx, right - x1, tMin, tMax) ?: return false
        tMin = r2.first; tMax = r2.second

        // Top boundary
        val r3 = clipSlab(-dy, y1 - y, tMin, tMax) ?: return false
        tMin = r3.first; tMax = r3.second

        // Bottom boundary
        val r4 = clipSlab(dy, bottom - y1, tMin, tMax) ?: return false
        tMin = r4.first; tMax = r4.second

        return tMin <= tMax
    }

    fun toPixelRect(canvasW: Float, canvasH: Float): RectF {
        return RectF(
            x      * canvasW,
            y      * canvasH,
            right  * canvasW,
            bottom * canvasH
        )
    }

    private fun clipSlab(p: Float, q: Float, t0: Float, t1: Float): Pair<Float, Float>? {
        if (p == 0f) {
            return if (q < 0f) null else Pair(t0, t1)
        }
        val r = q / p
        return if (p < 0f) {
            val newT0 = max(t0, r)
            if (newT0 > t1) null else Pair(newT0, t1)
        } else {
            val newT1 = min(t1, r)
            if (t0 > newT1) null else Pair(t0, newT1)
        }
    }
}
