package com.example.mosquitolaser.game.stages

import com.example.mosquitolaser.game.objects.LaserSource
import com.example.mosquitolaser.game.objects.Mirror
import com.example.mosquitolaser.game.objects.Mosquito
import com.example.mosquitolaser.game.objects.Obstacle

/**
 * Visual / audio theme applied to an entire world (6 stages × 10 stages each).
 */
enum class WorldTheme {
    /** World 1 – night sky, stars background, quiet ambient. */
    NIGHT,

    /** World 2 – urban rooftop setting, city skyline. */
    CITY,

    /** World 3 – dense jungle canopy, leafy obstacles. */
    JUNGLE,

    /** World 4 – industrial factory floor, conveyor belts & pipes. */
    FACTORY,

    /** World 5 – volcanic landscape, lava flows, dramatic lighting. */
    VOLCANO,

    /** World 6 – zero-gravity space station, stars and nebulae. */
    SPACE
}

/**
 * Complete description of a single stage/puzzle.
 *
 * Coordinates use a normalised [0,1]×[0,1] grid where
 *   (0,0) = top-left corner, (1,1) = bottom-right corner.
 *
 * @param stageNumber   Absolute stage index (1–60).
 * @param worldNumber   World this stage belongs to (1–6).
 * @param laserSource   Emitter position and initial firing angle.
 * @param mirrors       All mirrors present on the board at stage start.
 * @param mosquitoes    Target mosquitoes the player must zap.
 * @param obstacles     Optional static or dynamic obstacles.
 * @param condition     Optional win/play constraints (time limit, mirror limit …).
 * @param worldTheme    Rendering theme for backgrounds, particles, and SFX.
 * @param bgmTrack      R.raw resource ID for the background music track
 *                      (0 = silence / not yet assigned).
 */
data class StageData(
    val stageNumber: Int,
    val worldNumber: Int,
    val laserSource: LaserSource,
    val mirrors: List<Mirror>,
    val mosquitoes: List<Mosquito>,
    val obstacles: List<Obstacle> = emptyList(),
    val condition: StageCondition = StageCondition(),
    val worldTheme: WorldTheme,
    val bgmTrack: Int = 0
) {
    init {
        require(worldNumber in 1..6) { "worldNumber must be 1–6, got $worldNumber" }
        require(stageNumber in 1..60) { "stageNumber must be 1–60, got $stageNumber" }
    }

    /** Convenience: which stage within the world (1-based). */
    val stageInWorld: Int get() = stageNumber - (worldNumber - 1) * 10

    /** True when this stage has any special conditions beyond default. */
    val hasSpecialConditions: Boolean
        get() = condition.hasTimeLimit()
                || condition.hasMirrorLimit()
                || condition.hasMinReflections()
                || condition.hasForbiddenZones()
}
