package com.example.mosquitolaser.game.model

import android.graphics.PointF

/**
 * Represents the laser emitter placed at a fixed position on the game board.
 *
 * @param position  Normalised [0,1]×[0,1] position of the emitter.
 * @param angleDeg  Initial firing direction in degrees (0 = right, 90 = down,
 *                  180 = left, 270 = up), measured clockwise from the positive
 *                  X axis, following Android canvas conventions.
 */
data class LaserSource(
    val position: PointF,
    val angleDeg: Float
)
