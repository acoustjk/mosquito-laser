package com.example.mosquitolaser.game.model

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import kotlin.math.PI

/**
 * A reflective mirror the player can rotate (and optionally reposition).
 *
 * All angles are in degrees, following the standard math convention used
 * across the game engine (0° = horizontal surface; 45° = tilted so that a
 * left-to-right beam reflects downward).
 *
 * @param position      Normalised [0,1]×[0,1] centre of the mirror.
 * @param angleDeg      Current orientation of the mirror surface in degrees.
 * @param minAngleDeg   Lower bound of the rotation range the player is
 *                      allowed to use.
 * @param maxAngleDeg   Upper bound of the rotation range.
 * @param isMovable     Whether the player can reposition (drag) this mirror.
 *                      If false only rotation within [minAngle, maxAngle]
 *                      is permitted.
 * @param length        Half-length of the mirror expressed in normalised units.
 */
data class Mirror(
    val position: PointF,
    val angleDeg: Float,
    val minAngleDeg: Float = angleDeg - 60f,
    val maxAngleDeg: Float = angleDeg + 60f,
    val isMovable: Boolean = true,
    val length: Float = 0.06f
) {
    /**
     * Given an incoming laser direction [inAngleDeg] (degrees, CW from +X),
     * returns the outgoing reflected direction produced by this mirror surface.
     *
     * The mirror surface makes angle [angleDeg] with the X axis.
     * Reflection formula: outAngle = 2 * mirrorAngle - inAngle
     */
    fun reflect(inAngleDeg: Float): Float {
        // Normalise result to [0, 360)
        var out = (2f * angleDeg - inAngleDeg) % 360f
        if (out < 0f) out += 360f
        return out
    }

    /** Clamps [angleDeg] to the mirror's allowed rotation range. */
    fun clampAngle(requestedAngle: Float): Float =
        requestedAngle.coerceIn(minAngleDeg, maxAngleDeg)
}
