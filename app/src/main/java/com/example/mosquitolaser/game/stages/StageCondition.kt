package com.example.mosquitolaser.game.stages

import android.graphics.RectF

/**
 * Defines optional win/play conditions layered on top of a stage.
 * All fields have permissive defaults so simple stages need no customisation.
 *
 * @param timeLimitSeconds   Countdown in seconds; 0 means no time limit.
 * @param maxMovableMirrors  Maximum number of mirrors the player may reposition;
 *                           Int.MAX_VALUE means unlimited.
 * @param minReflections     Minimum laser-bounce count required before a kill
 *                           counts (e.g. "at least 2 reflections").
 * @param forbiddenZones     Normalised [0,1]×[0,1] rectangles the laser beam
 *                           must never pass through.
 */
data class StageCondition(
    val timeLimitSeconds: Int = 0,
    val maxMovableMirrors: Int = Int.MAX_VALUE,
    val minReflections: Int = 0,
    val forbiddenZones: List<RectF> = emptyList()
) {
    // ── Convenience predicates ────────────────────────────────────────────────

    fun hasTimeLimit(): Boolean = timeLimitSeconds > 0
    fun hasMirrorLimit(): Boolean = maxMovableMirrors < Int.MAX_VALUE
    fun hasMinReflections(): Boolean = minReflections > 0
    fun hasForbiddenZones(): Boolean = forbiddenZones.isNotEmpty()

    /**
     * Returns a list of human-readable strings describing every active
     * condition. If no conditions are active the list is empty (tutorial / basic stages).
     */
    fun getConditionDescriptions(): List<String> {
        val descriptions = mutableListOf<String>()

        if (hasTimeLimit()) {
            val minutes = timeLimitSeconds / 60
            val seconds = timeLimitSeconds % 60
            val formatted = if (minutes > 0) {
                "%d:%02d".format(minutes, seconds)
            } else {
                "${seconds}s"
            }
            descriptions += "Clear within $formatted"
        }

        if (hasMirrorLimit()) {
            val noun = if (maxMovableMirrors == 1) "mirror" else "mirrors"
            descriptions += "Move at most $maxMovableMirrors $noun"
        }

        if (hasMinReflections()) {
            val noun = if (minReflections == 1) "reflection" else "reflections"
            descriptions += "Laser needs at least $minReflections $noun"
        }

        if (hasForbiddenZones()) {
            val noun = if (forbiddenZones.size == 1) "zone" else "zones"
            descriptions += "Avoid ${forbiddenZones.size} forbidden $noun"
        }

        return descriptions
    }
}
