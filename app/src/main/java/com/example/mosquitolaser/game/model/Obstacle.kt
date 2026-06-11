package com.example.mosquitolaser.game.model

import android.graphics.RectF

/**
 * A solid obstacle on the game board that blocks / absorbs the laser beam.
 *
 * @param bounds    Normalised [0,1]×[0,1] rectangle occupied by the obstacle.
 * @param type      Visual / behaviour category of this obstacle.
 */
data class Obstacle(
    val bounds: RectF,
    val type: Type = Type.WALL
) {
    enum class Type {
        /** Completely absorbs the laser — beam is terminated. */
        WALL,

        /** Semi-transparent barrier that reduces laser power but does not stop it. */
        GLASS,

        /** Reflects the laser like a mirror but at a fixed angle (90° reflection). */
        REFLECTOR,

        /** Moving obstacle that periodically opens and closes a gap. */
        GATE
    }
}
