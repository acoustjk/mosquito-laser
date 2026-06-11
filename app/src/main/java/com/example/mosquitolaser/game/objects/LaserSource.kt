package com.example.mosquitolaser.game.objects

import android.graphics.PointF

/**
 * Represents a laser emitter on the game grid.
 *
 * @param x      Normalized horizontal position (0.0 = left edge, 1.0 = right edge).
 * @param y      Normalized vertical position (0.0 = top edge, 1.0 = bottom edge).
 * @param angle  Emission direction in degrees (0 = right, 90 = down, 180 = left, 270 = up).
 * @param color  ARGB color of the laser beam. Defaults to bright cyan (0xFF00FFFF).
 */
data class LaserSource(
    val x: Float,
    val y: Float,
    val angle: Float,
    val color: Int = 0xFF00FFFF.toInt()
) {
    /**
     * Converts the normalized position to canvas pixel coordinates.
     *
     * @param canvasW Width of the canvas in pixels.
     * @param canvasH Height of the canvas in pixels.
     * @return A [PointF] with the corresponding pixel coordinates.
     */
    fun toPixel(canvasW: Float, canvasH: Float): PointF {
        return PointF(x * canvasW, y * canvasH)
    }
}
