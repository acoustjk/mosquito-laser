package com.example.mosquitolaser.game.model

import android.graphics.PointF

/**
 * A mosquito target on the game board.
 *
 * @param position      Starting normalised [0,1]×[0,1] position.
 * @param movementType  How the mosquito moves during gameplay.
 * @param speed         Movement speed in normalised units per second.
 * @param moveRange     Half-amplitude of the movement path in normalised units
 *                      (used for LINEAR and CIRCULAR modes).
 * @param radius        Hit-circle radius in normalised units.
 */
data class Mosquito(
    val position: PointF,
    val movementType: MovementType = MovementType.STATIC,
    val speed: Float = 0f,
    val moveRange: Float = 0.1f,
    val radius: Float = 0.04f
) {
    enum class MovementType {
        /** Does not move; simplest target. */
        STATIC,

        /**
         * Oscillates back-and-forth horizontally (or vertically, depending on
         * stage design) by [moveRange] normalised units.
         */
        LINEAR,

        /**
         * Orbits its starting position in a circle of radius [moveRange].
         */
        CIRCULAR,

        /**
         * Moves in an erratic, unpredictable path (highest difficulty).
         */
        ERRATIC
    }
}
