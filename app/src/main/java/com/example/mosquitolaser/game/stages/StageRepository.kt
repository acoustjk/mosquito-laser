@file:Suppress("MagicNumber")

package com.example.mosquitolaser.game.stages

import com.example.mosquitolaser.game.objects.LaserSource
import com.example.mosquitolaser.game.objects.Mirror
import com.example.mosquitolaser.game.objects.Mosquito
import com.example.mosquitolaser.game.objects.Obstacle
import android.graphics.RectF

// ─────────────────────────────────────────────────────────────────────────────
//  COORDINATE SYSTEM
//  (0,0) = top-left   (1,0) = top-right
//  (0,1) = bot-left   (1,1) = bot-right
//
//  LASER ANGLES  (degrees, clockwise from +X axis)
//    0°  = firing RIGHT
//   90°  = firing DOWN
//  180°  = firing LEFT
//  270°  = firing UP
//
//  MIRROR SURFACE ANGLES
//   45° – beam going right deflects downward; beam going down deflects right
//  135° – beam going right deflects upward;   beam going down deflects left
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Central registry of all 60 stages.
 *
 * Stages are stored 0-indexed internally; all public API is 1-based.
 */
object StageRepository {

    // ─────────────────────────────────────────────────────────────────────────
    //  Compact builder functions – keep stage definitions readable
    // ─────────────────────────────────────────────────────────────────────────

    private fun src(x: Float, y: Float, angle: Float) =
        LaserSource(x, y, angle)

    /**
     * Mirror builder.
     * [id]     – unique per stage (reuse across stages is fine; engine resolves by list index)
     * [angle]  – surface orientation in degrees
     * [range]  – ± rotation range the player is allowed (0 = fixed)
     * [movable] – whether the player may reposition (drag) the mirror
     */
    private fun mirror(
        id: Int,
        x: Float,
        y: Float,
        angle: Float,
        range: Float = 60f,
        movable: Boolean = true,
        size: Float = 0.06f
    ) = Mirror(
        id = id,
        x = x,
        y = y,
        angle = angle,
        minAngle = angle - range,
        maxAngle = angle + range,
        isMovable = movable,
        size = size
    )

    // Mosquito.MovementType lives inside a companion object
    private val STATIC   = Mosquito.MovementType.STATIC
    private val LINEAR   = Mosquito.MovementType.LINEAR
    private val CIRCULAR = Mosquito.MovementType.CIRCULAR
    private val RANDOM   = Mosquito.MovementType.RANDOM   // "erratic" in design docs

    private fun mq(id: Int, x: Float, y: Float) =
        Mosquito(id, x, y, STATIC)

    private fun mqL(id: Int, x: Float, y: Float, speed: Float = 1.5f, range: Float = 0.10f) =
        Mosquito(id, x, y, LINEAR, speed, range)

    private fun mqC(id: Int, x: Float, y: Float, speed: Float = 1.8f, range: Float = 0.08f) =
        Mosquito(id, x, y, CIRCULAR, speed, range)

    private fun mqE(id: Int, x: Float, y: Float, speed: Float = 2.2f, range: Float = 0.10f) =
        Mosquito(id, x, y, RANDOM, speed, range)

    /** Wall obstacle – fully blocks laser */
    private fun wall(x: Float, y: Float, w: Float, h: Float) =
        Obstacle(x, y, w, h, isSemiTransparent = false)

    /** Glass obstacle – semi-transparent, laser passes through */
    private fun glass(x: Float, y: Float, w: Float, h: Float) =
        Obstacle(x, y, w, h, isSemiTransparent = true)

    /** Forbidden zone RectF (normalised). */
    private fun zone(x1: Float, y1: Float, x2: Float, y2: Float) =
        RectF(x1, y1, x2, y2)

    // ─────────────────────────────────────────────────────────────────────────
    //  ALL 60 STAGES
    // ─────────────────────────────────────────────────────────────────────────

    val stages: List<StageData> = listOf(

        // ══════════════════════════════════════════════════════════════════════
        //  WORLD 1 – NIGHT  (stages 1-10)
        //  REFLECTION RULES (R = 2*M - A, y-axis DOWN):
        //   45° mirror  : right(0°)↔down(90°),  left(180°)↔up(270°)
        //   135° mirror : right(0°)↔up(270°),   left(180°)↔down(90°)
        //
        //  Starting angles are OFFSET from solution to prevent auto-clear.
        //  Solution angle is always within [startAngle - range, startAngle + range].
        // ══════════════════════════════════════════════════════════════════════

        // ── Stage 1 – Tutorial ────────────────────────────────────────────────
        // Laser →right. Mirror must be rotated to 45° to deflect beam DOWN → mosquito.
        // Start at 80° (off-target). Solution: 45°. Range [20°,140°] includes 45°.
        StageData(
            stageNumber = 1, worldNumber = 1,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.5f, 0.5f, angle = 80f, range = 60f)
            ),
            mosquitoes = listOf(mq(1, 0.5f, 0.85f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 2 ───────────────────────────────────────────────────────────
        // Laser →right at y=0.3. Rotate mirror to 45° → beam goes DOWN → mosquito.
        // Start at 78°. Range [33°,123°] includes 45°.
        StageData(
            stageNumber = 2, worldNumber = 1,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(
                mirror(1, 0.6f, 0.3f, 78f, 45f)
            ),
            mosquitoes = listOf(mq(1, 0.6f, 0.75f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 3 – Two bounces: right→down→right ───────────────────────────
        // Both mirrors need 45° (the "\" shape).
        // M1: right→down. M2: down→right. Mosquito to right of M2.
        // (Previous bug: M2 was 135°, which sends down→LEFT not right.)
        StageData(
            stageNumber = 3, worldNumber = 1,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.2f,  75f, 45f),  // sol=45°, right→down
                mirror(2, 0.35f, 0.65f, 75f, 45f)   // sol=45°, down→right (was 135°!)
            ),
            mosquitoes = listOf(mq(1, 0.78f, 0.65f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 4 – Two bounces: down→right→down ────────────────────────────
        // Laser from top. Both mirrors need 45°.
        // M1: down→right. M2: right→down. Mosquito below M2.
        // (Previous bug: M1 was 135°, range [90°,180°] → solution 45° was OUTSIDE range!)
        StageData(
            stageNumber = 4, worldNumber = 1,
            laserSource = src(0.25f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.35f, 75f, 50f),  // sol=45°, down→right (was 135°!)
                mirror(2, 0.72f, 0.35f, 75f, 50f)   // sol=45°, right→down
            ),
            mosquitoes = listOf(mq(1, 0.72f, 0.82f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 5 – Chain: down→right(mq1 in path)→down(mq2) ───────────────
        // Source from top. Beam hits M1(45°)→right, passes mosquito 1 in mid-air,
        // hits M2(45°)→down, kills mosquito 2. Both mirrors movable.
        StageData(
            stageNumber = 5, worldNumber = 1,
            laserSource = src(0.2f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.3f, 75f, 50f),   // sol=45°, down→right
                mirror(2, 0.72f, 0.3f, 75f, 50f)   // sol=45°, right→down
            ),
            mosquitoes = listOf(
                mq(1, 0.46f, 0.3f),    // in the horizontal beam between M1 and M2
                mq(2, 0.72f, 0.72f)    // below M2
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 6 – Three-mirror zigzag: right→down→right→down ─────────────
        // All three mirrors: 45°. "S-shaped" laser path.
        StageData(
            stageNumber = 6, worldNumber = 1,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.5f,  75f, 50f),  // sol=45°, right→down
                mirror(2, 0.25f, 0.75f, 75f, 50f),  // sol=45°, down→right (was 135°!)
                mirror(3, 0.70f, 0.75f, 75f, 50f)   // sol=45°, right→down
            ),
            mosquitoes = listOf(mq(1, 0.70f, 0.90f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 7 – Three mirrors, two mosquitoes ───────────────────────────
        // Source top. M1(45°) down→right. M2(45°) right→down.
        // Mosquito 1 sits in the horizontal beam between M1 and M2.
        // M3(135°) sends the downward beam LEFT → mosquito 2.
        StageData(
            stageNumber = 7, worldNumber = 1,
            laserSource = src(0.3f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.3f,  0.4f,  75f, 50f),  // sol=45°,  down→right
                mirror(2, 0.72f, 0.4f,  75f, 50f),  // sol=45°,  right→down
                mirror(3, 0.72f, 0.72f, 100f, 40f)  // sol=135°, down→left
            ),
            mosquitoes = listOf(
                mq(1, 0.51f, 0.4f),   // in horizontal beam (M1→M2)
                mq(2, 0.18f, 0.72f)   // in left beam from M3
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 8 – Four mirrors, two mosquitoes ────────────────────────────
        // right→down [mq1 between M1&M2] →right→down→left→ mq2
        StageData(
            stageNumber = 8, worldNumber = 1,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.15f, 75f, 45f),  // sol=45°,  right→down
                mirror(2, 0.25f, 0.55f, 75f, 45f),  // sol=45°,  down→right (was 135°!)
                mirror(3, 0.65f, 0.55f, 75f, 45f),  // sol=45°,  right→down
                mirror(4, 0.65f, 0.82f, 100f, 40f)  // sol=135°, down→left
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.35f),  // in first downward beam (between M1 and M2)
                mq(2, 0.18f, 0.82f)   // in leftward beam from M4
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 9 – Four mirrors, tight angles, two mosquitoes ──────────────
        // Source RIGHT edge going LEFT (180°).
        // Path: left→[M1 135°]→down→[M2 135°]→left→[M3 135°]→down
        // Mosquito 1 in first down-beam (between M1 and M2)
        // Mosquito 2 below M3
        // Verify: left(180°)→M1(135°): R=2*135-180=90(down) ✓
        //         down(90°)→M2(135°): R=2*135-90=180(left) ✓
        //         left(180°)→M3(135°): R=2*135-180=90(down) ✓
        StageData(
            stageNumber = 9, worldNumber = 1,
            laserSource = src(1f, 0.5f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.5f,  100f, 40f),  // sol=135°, left→down
                mirror(2, 0.75f, 0.75f, 100f, 40f),  // sol=135°, down→left
                mirror(3, 0.35f, 0.75f, 100f, 40f)   // sol=135°, left→down
            ),
            mosquitoes = listOf(
                mq(1, 0.75f, 0.62f),  // in downward beam between M1 and M2
                mq(2, 0.35f, 0.88f)   // below M3
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // ── Stage 10 – Four mirrors, three mosquitoes chain, min 2 reflections ─
        // Source left→ right→[M1 45°]→down (mq1 mid-beam)→[M2 45°]→right (mq2 mid-beam)→[M3 45°]→down (mq3)
        // Tight angles, requires at least 2 reflections.
        // All angles 45°: right↔down pattern.
        StageData(
            stageNumber = 10, worldNumber = 1,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.22f, 0.15f, 75f, 35f),  // sol=45°, right→down
                mirror(2, 0.22f, 0.55f, 75f, 35f),  // sol=45°, down→right
                mirror(3, 0.60f, 0.55f, 75f, 35f)   // sol=45°, right→down
            ),
            mosquitoes = listOf(
                mq(1, 0.22f, 0.35f),  // in first down-beam (M1→M2)
                mq(2, 0.41f, 0.55f),  // in horizontal beam (M2→M3)
                mq(3, 0.60f, 0.80f)   // below M3
            ),
            condition = StageCondition(minReflections = 2),
            worldTheme = WorldTheme.NIGHT
        ),

        // ══════════════════════════════════════════════════════════════════════
        //  WORLD 2 – CITY  (stages 11-20)
        //  Obstacles introduced; glass panels and walls.
        // ══════════════════════════════════════════════════════════════════════

        // ── Stage 11 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 11, worldNumber = 2,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(mirror(1, 0.55f, 0.3f, 45f, 50f)),
            mosquitoes = listOf(mq(1, 0.55f, 0.78f)),
            obstacles = listOf(wall(0.30f, 0.20f, 0.10f, 0.25f)),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 12 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 12, worldNumber = 2,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.5f,   45f, 45f),
                mirror(2, 0.3f, 0.75f, 135f, 45f)
            ),
            mosquitoes = listOf(mq(1, 0.75f, 0.75f)),
            obstacles = listOf(wall(0.55f, 0.60f, 0.10f, 0.30f)),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 13 – Glass obstacle ─────────────────────────────────────────
        StageData(
            stageNumber = 13, worldNumber = 2,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.45f, 0.4f,  45f, 40f),
                mirror(2, 0.45f, 0.7f, 135f, 40f)
            ),
            mosquitoes = listOf(mq(1, 0.80f, 0.7f)),
            obstacles = listOf(glass(0.58f, 0.55f, 0.10f, 0.30f)),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 14 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 14, worldNumber = 2,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.25f, 135f, 40f),
                mirror(2, 0.2f,  0.25f,  45f, 40f),
                mirror(3, 0.2f,  0.65f, 135f, 40f)
            ),
            mosquitoes = listOf(
                mq(1, 0.2f,  0.88f),
                mq(2, 0.55f, 0.65f)
            ),
            obstacles = listOf(
                wall(0.55f, 0.10f, 0.15f, 0.12f),
                wall(0.30f, 0.38f, 0.12f, 0.12f)
            ),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 15 – Corridor-style ─────────────────────────────────────────
        StageData(
            stageNumber = 15, worldNumber = 2,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.38f, 0.5f,   45f, 35f),
                mirror(2, 0.38f, 0.78f, 135f, 35f),
                mirror(3, 0.72f, 0.78f,  45f, 35f)
            ),
            mosquitoes = listOf(mq(1, 0.72f, 0.92f)),
            obstacles = listOf(
                wall(0.10f, 0.38f, 0.15f, 0.24f),
                wall(0.50f, 0.62f, 0.10f, 0.06f),
                wall(0.50f, 0.88f, 0.10f, 0.06f)
            ),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 16 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 16, worldNumber = 2,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f,  0.2f,  45f, 35f),
                mirror(2, 0.3f,  0.5f, 135f, 35f),
                mirror(3, 0.6f,  0.5f,  45f, 35f),
                mirror(4, 0.6f,  0.78f,135f, 35f)
            ),
            mosquitoes = listOf(
                mq(1, 0.3f,  0.85f),
                mq(2, 0.6f,  0.92f),
                mq(3, 0.88f, 0.78f)
            ),
            obstacles = listOf(
                wall(0.42f, 0.10f, 0.10f, 0.08f),
                wall(0.42f, 0.62f, 0.10f, 0.08f)
            ),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 17 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 17, worldNumber = 2,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.22f, 0.5f,   45f, 30f),
                mirror(2, 0.22f, 0.78f, 135f, 30f),
                mirror(3, 0.55f, 0.78f,  45f, 30f),
                mirror(4, 0.55f, 0.55f, 135f, 30f),
                mirror(5, 0.82f, 0.55f,  45f, 30f)
            ),
            mosquitoes = listOf(
                mq(1, 0.82f, 0.82f),
                mq(2, 0.92f, 0.55f)
            ),
            obstacles = listOf(
                wall(0.30f, 0.38f, 0.08f, 0.28f),
                wall(0.65f, 0.38f, 0.08f, 0.08f),
                wall(0.65f, 0.85f, 0.08f, 0.07f)
            ),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 18 – Five mirrors, min 3 reflections ────────────────────────
        StageData(
            stageNumber = 18, worldNumber = 2,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.2f,  0.5f,   45f, 30f),
                mirror(2, 0.2f,  0.75f, 135f, 30f),
                mirror(3, 0.45f, 0.75f,  45f, 30f),
                mirror(4, 0.45f, 0.45f, 135f, 30f),
                mirror(5, 0.72f, 0.45f,  45f, 30f)
            ),
            mosquitoes = listOf(mq(1, 0.72f, 0.85f)),
            obstacles = listOf(
                wall(0.55f, 0.30f, 0.07f, 0.12f),
                wall(0.30f, 0.58f, 0.07f, 0.10f)
            ),
            condition = StageCondition(minReflections = 3),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 19 – Six mirrors, three mosquitoes, two glass obstacles ──────
        StageData(
            stageNumber = 19, worldNumber = 2,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.18f,  45f, 30f),
                mirror(2, 0.78f, 0.18f, 135f, 30f),
                mirror(3, 0.78f, 0.55f,  45f, 30f),
                mirror(4, 0.5f,  0.55f, 135f, 30f),
                mirror(5, 0.5f,  0.78f,  45f, 30f),
                mirror(6, 0.25f, 0.78f, 135f, 30f)
            ),
            mosquitoes = listOf(
                mq(1, 0.78f, 0.85f),
                mq(2, 0.5f,  0.92f),
                mq(3, 0.1f,  0.78f)
            ),
            obstacles = listOf(
                glass(0.60f, 0.30f, 0.10f, 0.10f),
                glass(0.30f, 0.62f, 0.12f, 0.10f)
            ),
            condition = StageCondition(minReflections = 3),
            worldTheme = WorldTheme.CITY
        ),

        // ── Stage 20 – City boss ──────────────────────────────────────────────
        StageData(
            stageNumber = 20, worldNumber = 2,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.18f, 0.5f,   45f, 25f),
                mirror(2, 0.18f, 0.78f, 135f, 25f),
                mirror(3, 0.42f, 0.78f,  45f, 25f),
                mirror(4, 0.42f, 0.52f, 135f, 25f),
                mirror(5, 0.65f, 0.52f,  45f, 25f),
                mirror(6, 0.65f, 0.78f, 135f, 25f)
            ),
            mosquitoes = listOf(
                mq(1, 0.18f, 0.93f),
                mq(2, 0.65f, 0.93f),
                mq(3, 0.88f, 0.52f)
            ),
            obstacles = listOf(
                wall(0.28f, 0.38f, 0.07f, 0.12f),
                wall(0.28f, 0.85f, 0.07f, 0.09f),
                wall(0.72f, 0.60f, 0.08f, 0.08f)
            ),
            condition = StageCondition(minReflections = 4),
            worldTheme = WorldTheme.CITY
        ),

        // ══════════════════════════════════════════════════════════════════════
        //  WORLD 3 – JUNGLE  (stages 21-30)
        //  Moving mosquitoes; mirror-move limits added at stage 24.
        // ══════════════════════════════════════════════════════════════════════

        // ── Stage 21 – One linear mosquito ────────────────────────────────────
        StageData(
            stageNumber = 21, worldNumber = 3,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(mirror(1, 0.5f, 0.4f, 45f, 55f)),
            mosquitoes = listOf(mqL(1, 0.5f, 0.75f, 1.2f, 0.12f)),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 22 – Two mirrors, one linear mosquito ───────────────────────
        StageData(
            stageNumber = 22, worldNumber = 3,
            laserSource = src(0f, 0.35f, 0f),
            mirrors = listOf(
                mirror(1, 0.4f, 0.35f,  45f, 45f),
                mirror(2, 0.4f, 0.65f, 135f, 45f)
            ),
            mosquitoes = listOf(mqL(1, 0.78f, 0.65f, 1.4f, 0.10f)),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 23 – Three mirrors, two linear mosquitoes ───────────────────
        StageData(
            stageNumber = 23, worldNumber = 3,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.28f, 135f, 40f),
                mirror(2, 0.2f,  0.28f,  45f, 40f),
                mirror(3, 0.2f,  0.65f, 135f, 40f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.2f,  0.82f, 1.4f, 0.08f),
                mqL(2, 0.68f, 0.65f, 1.2f, 0.10f)
            ),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 24 – Mirror limit = 2 introduced ───────────────────────────
        StageData(
            stageNumber = 24, worldNumber = 3,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f,  0.5f,   45f, 35f),
                mirror(2, 0.3f,  0.75f, 135f, 35f),
                mirror(3, 0.65f, 0.75f,  45f, 10f, movable = false)
            ),
            mosquitoes = listOf(mqL(1, 0.65f, 0.90f, 1.5f, 0.08f)),
            condition = StageCondition(maxMovableMirrors = 2),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 25 – Circular mosquito, limit 2 ─────────────────────────────
        StageData(
            stageNumber = 25, worldNumber = 3,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.3f,  45f, 40f),
                mirror(2, 0.35f, 0.7f, 135f, 40f),
                mirror(3, 0.70f, 0.7f,  45f, 15f, movable = false)
            ),
            mosquitoes = listOf(mqC(1, 0.70f, 0.88f, 1.7f, 0.07f)),
            condition = StageCondition(maxMovableMirrors = 2),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 26 – Static + linear, mirror limit 3 ───────────────────────
        StageData(
            stageNumber = 26, worldNumber = 3,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.22f, 135f, 40f),
                mirror(2, 0.25f, 0.22f,  45f, 40f),
                mirror(3, 0.25f, 0.58f, 135f, 40f),
                mirror(4, 0.60f, 0.58f,  45f, 15f, movable = false)
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.78f),
                mqL(2, 0.60f, 0.82f, 1.4f, 0.09f)
            ),
            condition = StageCondition(maxMovableMirrors = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 27 – Two circular mosquitoes, one obstacle, limit 3 ─────────
        StageData(
            stageNumber = 27, worldNumber = 3,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.22f, 0.5f,   45f, 35f),
                mirror(2, 0.22f, 0.75f, 135f, 35f),
                mirror(3, 0.55f, 0.75f,  45f, 35f),
                mirror(4, 0.55f, 0.45f, 135f, 35f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.22f, 0.9f, 1.8f, 0.07f),
                mqC(2, 0.82f, 0.45f, 1.8f, 0.07f)
            ),
            obstacles = listOf(wall(0.35f, 0.38f, 0.07f, 0.24f)),
            condition = StageCondition(maxMovableMirrors = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 28 – Five mirrors, mixed movement, limit 3 ─────────────────
        StageData(
            stageNumber = 28, worldNumber = 3,
            laserSource = src(1f, 0.3f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.3f,  135f, 35f),
                mirror(2, 0.75f, 0.65f,  45f, 35f),
                mirror(3, 0.45f, 0.65f, 135f, 35f),
                mirror(4, 0.45f, 0.35f,  45f, 15f, movable = false),
                mirror(5, 0.2f,  0.35f, 135f, 35f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.2f,  0.15f, 1.4f, 0.10f),
                mqC(2, 0.45f, 0.82f, 1.6f, 0.08f),
                mq(3, 0.92f, 0.65f)
            ),
            obstacles = listOf(wall(0.56f, 0.48f, 0.09f, 0.07f)),
            condition = StageCondition(maxMovableMirrors = 3, minReflections = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 29 – Random (erratic) mosquito introduced ──────────────────
        StageData(
            stageNumber = 29, worldNumber = 3,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f,  0.5f,   45f, 35f),
                mirror(2, 0.3f,  0.72f, 135f, 35f),
                mirror(3, 0.6f,  0.72f,  45f, 25f),
                mirror(4, 0.6f,  0.5f,  135f, 25f)
            ),
            mosquitoes = listOf(
                mqE(1, 0.82f, 0.5f,  2.0f, 0.10f),
                mq(2, 0.6f,  0.88f)
            ),
            obstacles = listOf(
                wall(0.42f, 0.38f, 0.08f, 0.07f),
                glass(0.42f, 0.75f, 0.08f, 0.07f)
            ),
            condition = StageCondition(maxMovableMirrors = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ── Stage 30 – Jungle boss ────────────────────────────────────────────
        StageData(
            stageNumber = 30, worldNumber = 3,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.18f, 135f, 30f),
                mirror(2, 0.22f, 0.18f,  45f, 30f),
                mirror(3, 0.22f, 0.5f,  135f, 30f),
                mirror(4, 0.22f, 0.75f,  45f, 15f, movable = false),
                mirror(5, 0.55f, 0.5f,   45f, 30f),
                mirror(6, 0.55f, 0.75f, 135f, 15f, movable = false)
            ),
            mosquitoes = listOf(
                mqL(1, 0.22f, 0.92f, 1.6f, 0.09f),
                mqC(2, 0.55f, 0.92f, 1.8f, 0.07f),
                mqE(3, 0.78f, 0.5f,  2.0f, 0.11f)
            ),
            obstacles = listOf(
                wall(0.35f, 0.35f, 0.07f, 0.30f),
                glass(0.60f, 0.60f, 0.08f, 0.10f)
            ),
            condition = StageCondition(maxMovableMirrors = 3, minReflections = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ══════════════════════════════════════════════════════════════════════
        //  WORLD 4 – FACTORY  (stages 31-40)
        //  Time limits; forbidden zones from stage 38.
        // ══════════════════════════════════════════════════════════════════════

        // ── Stage 31 – 60-second limit ────────────────────────────────────────
        StageData(
            stageNumber = 31, worldNumber = 4,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.4f,   45f, 45f),
                mirror(2, 0.35f, 0.7f,  135f, 45f),
                mirror(3, 0.70f, 0.7f,   45f, 45f)
            ),
            mosquitoes = listOf(mq(1, 0.70f, 0.88f)),
            obstacles = listOf(wall(0.50f, 0.55f, 0.08f, 0.10f)),
            condition = StageCondition(timeLimitSeconds = 60),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 32 – 58 seconds, two mosquitoes ─────────────────────────────
        StageData(
            stageNumber = 32, worldNumber = 4,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.5f,   45f, 40f),
                mirror(2, 0.25f, 0.75f, 135f, 40f),
                mirror(3, 0.60f, 0.75f,  45f, 40f)
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.90f),
                mq(2, 0.60f, 0.90f)
            ),
            obstacles = listOf(wall(0.40f, 0.38f, 0.08f, 0.24f)),
            condition = StageCondition(timeLimitSeconds = 58),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 33 – 55 seconds, linear mosquito ────────────────────────────
        StageData(
            stageNumber = 33, worldNumber = 4,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.25f,  45f, 40f),
                mirror(2, 0.80f, 0.25f, 135f, 40f),
                mirror(3, 0.80f, 0.60f,  45f, 40f)
            ),
            mosquitoes = listOf(mqL(1, 0.80f, 0.82f, 1.7f, 0.09f)),
            obstacles = listOf(
                wall(0.62f, 0.12f, 0.08f, 0.10f),
                wall(0.62f, 0.42f, 0.08f, 0.10f)
            ),
            condition = StageCondition(timeLimitSeconds = 55),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 34 – 55 seconds, two linear mosquitoes, 4 mirrors ──────────
        StageData(
            stageNumber = 34, worldNumber = 4,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(
                mirror(1, 0.28f, 0.3f,   45f, 35f),
                mirror(2, 0.28f, 0.6f,  135f, 35f),
                mirror(3, 0.58f, 0.6f,   45f, 35f),
                mirror(4, 0.58f, 0.3f,  135f, 35f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.28f, 0.82f, 1.6f, 0.09f),
                mqL(2, 0.82f, 0.3f,  1.6f, 0.09f)
            ),
            obstacles = listOf(
                wall(0.38f, 0.18f, 0.08f, 0.10f),
                wall(0.38f, 0.68f, 0.08f, 0.10f)
            ),
            condition = StageCondition(timeLimitSeconds = 55),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 35 – 50 seconds, mirror limit 3, circular mosquito ──────────
        StageData(
            stageNumber = 35, worldNumber = 4,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.22f, 0.5f,   45f, 35f),
                mirror(2, 0.22f, 0.75f, 135f, 35f),
                mirror(3, 0.55f, 0.75f,  45f, 20f, movable = false),
                mirror(4, 0.55f, 0.45f, 135f, 35f)
            ),
            mosquitoes = listOf(mqC(1, 0.82f, 0.45f, 1.9f, 0.08f)),
            obstacles = listOf(wall(0.35f, 0.38f, 0.07f, 0.24f)),
            condition = StageCondition(timeLimitSeconds = 50, maxMovableMirrors = 3),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 36 – 50 seconds, 5 mirrors, 2 mosquitoes ───────────────────
        StageData(
            stageNumber = 36, worldNumber = 4,
            laserSource = src(1f, 0.4f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.4f,  135f, 32f),
                mirror(2, 0.75f, 0.7f,   45f, 32f),
                mirror(3, 0.45f, 0.7f,  135f, 32f),
                mirror(4, 0.45f, 0.4f,   45f, 32f),
                mirror(5, 0.20f, 0.4f,  135f, 32f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.20f, 0.20f, 1.7f, 0.10f),
                mqC(2, 0.92f, 0.70f, 1.8f, 0.08f)
            ),
            obstacles = listOf(
                wall(0.55f, 0.55f, 0.09f, 0.07f),
                wall(0.28f, 0.55f, 0.09f, 0.07f)
            ),
            condition = StageCondition(timeLimitSeconds = 50, maxMovableMirrors = 4),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 37 – 45 seconds, 3 mosquitoes, tight angles ────────────────
        StageData(
            stageNumber = 37, worldNumber = 4,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.2f,   45f, 28f),
                mirror(2, 0.80f, 0.2f,  135f, 28f),
                mirror(3, 0.80f, 0.55f,  45f, 28f),
                mirror(4, 0.5f,  0.55f, 135f, 28f),
                mirror(5, 0.5f,  0.8f,   45f, 28f)
            ),
            mosquitoes = listOf(
                mq(1, 0.80f, 0.85f),
                mqL(2, 0.5f,  0.93f, 1.8f, 0.08f),
                mqC(3, 0.18f, 0.55f, 1.7f, 0.08f)
            ),
            obstacles = listOf(
                wall(0.60f, 0.35f, 0.10f, 0.07f),
                wall(0.30f, 0.65f, 0.10f, 0.07f)
            ),
            condition = StageCondition(timeLimitSeconds = 45, minReflections = 3),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 38 – 45 seconds, forbidden zone introduced ─────────────────
        StageData(
            stageNumber = 38, worldNumber = 4,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.28f, 0.5f,   45f, 35f),
                mirror(2, 0.28f, 0.78f, 135f, 35f),
                mirror(3, 0.62f, 0.78f,  45f, 35f)
            ),
            mosquitoes = listOf(mq(1, 0.62f, 0.92f)),
            obstacles = listOf(wall(0.42f, 0.62f, 0.08f, 0.08f)),
            condition = StageCondition(
                timeLimitSeconds = 45,
                forbiddenZones = listOf(zone(0.30f, 0.08f, 0.70f, 0.40f))
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 39 – 40 seconds, forbidden zone + mirror limit ─────────────
        StageData(
            stageNumber = 39, worldNumber = 4,
            laserSource = src(0f, 0.55f, 0f),
            mirrors = listOf(
                mirror(1, 0.30f, 0.55f,  45f, 32f),
                mirror(2, 0.30f, 0.78f, 135f, 32f),
                mirror(3, 0.62f, 0.78f,  45f, 18f, movable = false),
                mirror(4, 0.62f, 0.55f, 135f, 32f)
            ),
            mosquitoes = listOf(
                mq(1, 0.62f, 0.93f),
                mqL(2, 0.88f, 0.55f, 1.9f, 0.09f)
            ),
            obstacles = listOf(wall(0.44f, 0.40f, 0.08f, 0.08f)),
            condition = StageCondition(
                timeLimitSeconds = 40,
                maxMovableMirrors = 3,
                forbiddenZones = listOf(zone(0.05f, 0.05f, 0.50f, 0.45f))
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // ── Stage 40 – Factory boss ───────────────────────────────────────────
        StageData(
            stageNumber = 40, worldNumber = 4,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.18f, 0.5f,   45f, 25f),
                mirror(2, 0.18f, 0.78f, 135f, 25f),
                mirror(3, 0.42f, 0.78f,  45f, 25f),
                mirror(4, 0.42f, 0.52f, 135f, 25f),
                mirror(5, 0.65f, 0.52f,  45f, 25f),
                mirror(6, 0.65f, 0.78f, 135f, 20f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.18f, 0.92f, 2.0f, 0.08f),
                mqL(2, 0.42f, 0.92f, 1.8f, 0.09f),
                mqE(3, 0.88f, 0.52f, 2.2f, 0.10f)
            ),
            obstacles = listOf(
                wall(0.28f, 0.38f, 0.08f, 0.10f),
                wall(0.52f, 0.62f, 0.08f, 0.08f),
                glass(0.28f, 0.85f, 0.07f, 0.07f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 35,
                maxMovableMirrors = 5,
                minReflections = 4,
                forbiddenZones = listOf(zone(0.40f, 0.05f, 0.90f, 0.40f))
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // ══════════════════════════════════════════════════════════════════════
        //  WORLD 5 – VOLCANO  (stages 41-50)
        //  Very fast mosquitoes; tight angles; combined conditions.
        // ══════════════════════════════════════════════════════════════════════

        // ── Stage 41 – 55 seconds, fast circular mosquito ─────────────────────
        StageData(
            stageNumber = 41, worldNumber = 5,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.40f, 0.4f,   45f, 30f),
                mirror(2, 0.40f, 0.72f, 135f, 30f)
            ),
            mosquitoes = listOf(mqC(1, 0.75f, 0.72f, 2.5f, 0.08f)),
            obstacles = listOf(wall(0.55f, 0.28f, 0.09f, 0.10f)),
            condition = StageCondition(timeLimitSeconds = 55, minReflections = 2),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 42 – 50 seconds, 4 mirrors, two fast mosquitoes ────────────
        StageData(
            stageNumber = 42, worldNumber = 5,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.22f, 135f, 28f),
                mirror(2, 0.22f, 0.22f,  45f, 28f),
                mirror(3, 0.22f, 0.6f,  135f, 28f),
                mirror(4, 0.65f, 0.6f,   45f, 28f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.22f, 0.82f, 2.6f, 0.07f),
                mqL(2, 0.65f, 0.82f, 2.4f, 0.09f)
            ),
            obstacles = listOf(
                wall(0.35f, 0.38f, 0.07f, 0.10f),
                wall(0.35f, 0.65f, 0.07f, 0.10f)
            ),
            condition = StageCondition(timeLimitSeconds = 50, maxMovableMirrors = 3),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 43 – 48 seconds, erratic + circular, 5 mirrors ─────────────
        StageData(
            stageNumber = 43, worldNumber = 5,
            laserSource = src(1f, 0.5f, 180f),
            mirrors = listOf(
                mirror(1, 0.78f, 0.5f,  135f, 28f),
                mirror(2, 0.78f, 0.75f,  45f, 28f),
                mirror(3, 0.5f,  0.75f, 135f, 28f),
                mirror(4, 0.5f,  0.5f,   45f, 22f, movable = false),
                mirror(5, 0.28f, 0.5f,  135f, 28f)
            ),
            mosquitoes = listOf(
                mqE(1, 0.28f, 0.28f, 2.5f, 0.10f),
                mqC(2, 0.78f, 0.88f, 2.4f, 0.07f)
            ),
            obstacles = listOf(
                wall(0.60f, 0.60f, 0.08f, 0.08f),
                glass(0.36f, 0.62f, 0.08f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 48,
                maxMovableMirrors = 4,
                minReflections = 3
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 44 – 45 seconds, forbidden zone + fast enemies ─────────────
        StageData(
            stageNumber = 44, worldNumber = 5,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.5f,   45f, 25f),
                mirror(2, 0.25f, 0.78f, 135f, 25f),
                mirror(3, 0.58f, 0.78f,  45f, 25f),
                mirror(4, 0.58f, 0.5f,  135f, 25f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.25f, 0.93f, 2.8f, 0.07f),
                mqL(2, 0.82f, 0.5f,  2.6f, 0.09f)
            ),
            obstacles = listOf(wall(0.38f, 0.60f, 0.08f, 0.08f)),
            condition = StageCondition(
                timeLimitSeconds = 45,
                forbiddenZones = listOf(zone(0.30f, 0.05f, 0.80f, 0.42f))
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 45 – 43 seconds, 3 forbidden zones ──────────────────────────
        StageData(
            stageNumber = 45, worldNumber = 5,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.25f,  45f, 25f),
                mirror(2, 0.78f, 0.25f, 135f, 25f),
                mirror(3, 0.78f, 0.58f,  45f, 25f),
                mirror(4, 0.5f,  0.58f, 135f, 22f, movable = false),
                mirror(5, 0.25f, 0.58f,  45f, 25f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.78f, 0.82f, 2.7f, 0.08f),
                mqC(2, 0.25f, 0.82f, 2.5f, 0.07f),
                mq(3, 0.92f, 0.58f)
            ),
            obstacles = listOf(wall(0.60f, 0.38f, 0.08f, 0.08f)),
            condition = StageCondition(
                timeLimitSeconds = 43,
                maxMovableMirrors = 4,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.42f, 0.20f),
                    zone(0.60f, 0.05f, 0.95f, 0.20f),
                    zone(0.40f, 0.68f, 0.60f, 0.80f)
                )
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 46 – 42 seconds, 6 mirrors, 3 fast mosquitoes ──────────────
        StageData(
            stageNumber = 46, worldNumber = 5,
            laserSource = src(0f, 0.45f, 0f),
            mirrors = listOf(
                mirror(1, 0.18f, 0.45f,  45f, 22f),
                mirror(2, 0.18f, 0.72f, 135f, 22f),
                mirror(3, 0.40f, 0.72f,  45f, 22f),
                mirror(4, 0.40f, 0.45f, 135f, 22f),
                mirror(5, 0.62f, 0.45f,  45f, 22f),
                mirror(6, 0.62f, 0.72f, 135f, 18f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.18f, 0.90f, 2.9f, 0.07f),
                mqE(2, 0.40f, 0.90f, 2.7f, 0.09f),
                mqL(3, 0.88f, 0.45f, 2.9f, 0.08f)
            ),
            obstacles = listOf(
                wall(0.28f, 0.32f, 0.08f, 0.08f),
                wall(0.50f, 0.58f, 0.08f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 42,
                maxMovableMirrors = 5,
                minReflections = 4,
                forbiddenZones = listOf(zone(0.05f, 0.05f, 0.95f, 0.32f))
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 47 – 40 seconds, 6 mirrors, tight corridor ─────────────────
        StageData(
            stageNumber = 47, worldNumber = 5,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.22f, 0.5f,   45f, 20f),
                mirror(2, 0.22f, 0.78f, 135f, 20f),
                mirror(3, 0.50f, 0.78f,  45f, 20f),
                mirror(4, 0.50f, 0.5f,  135f, 20f),
                mirror(5, 0.75f, 0.5f,   45f, 20f),
                mirror(6, 0.75f, 0.78f, 135f, 18f, movable = false)
            ),
            mosquitoes = listOf(
                mqL(1, 0.22f, 0.93f, 2.9f, 0.08f),
                mqC(2, 0.50f, 0.93f, 2.8f, 0.07f),
                mqE(3, 0.92f, 0.78f, 2.7f, 0.10f)
            ),
            obstacles = listOf(
                wall(0.32f, 0.38f, 0.08f, 0.08f),
                wall(0.60f, 0.62f, 0.08f, 0.08f),
                glass(0.32f, 0.85f, 0.08f, 0.07f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 40,
                maxMovableMirrors = 5,
                forbiddenZones = listOf(zone(0.05f, 0.05f, 0.95f, 0.35f))
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 48 – 38 seconds, all-moving, 4 forbidden zones ─────────────
        StageData(
            stageNumber = 48, worldNumber = 5,
            laserSource = src(1f, 0.5f, 180f),
            mirrors = listOf(
                mirror(1, 0.78f, 0.5f,  135f, 20f),
                mirror(2, 0.78f, 0.75f,  45f, 20f),
                mirror(3, 0.50f, 0.75f, 135f, 20f),
                mirror(4, 0.50f, 0.5f,   45f, 20f),
                mirror(5, 0.25f, 0.5f,  135f, 20f),
                mirror(6, 0.25f, 0.75f,  45f, 18f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.78f, 0.90f, 3.0f, 0.07f),
                mqE(2, 0.50f, 0.90f, 2.8f, 0.09f),
                mqL(3, 0.10f, 0.75f, 3.0f, 0.08f)
            ),
            obstacles = listOf(
                wall(0.60f, 0.35f, 0.08f, 0.10f),
                wall(0.34f, 0.60f, 0.08f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 38,
                maxMovableMirrors = 5,
                minReflections = 4,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.30f),
                    zone(0.35f, 0.32f, 0.65f, 0.45f)
                )
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 49 – 35 seconds, 7 mirrors, extreme angles ─────────────────
        StageData(
            stageNumber = 49, worldNumber = 5,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.15f, 0.5f,   45f, 18f),
                mirror(2, 0.15f, 0.72f, 135f, 18f),
                mirror(3, 0.35f, 0.72f,  45f, 18f),
                mirror(4, 0.35f, 0.5f,  135f, 18f),
                mirror(5, 0.55f, 0.5f,   45f, 18f),
                mirror(6, 0.55f, 0.72f, 135f, 18f),
                mirror(7, 0.75f, 0.72f,  45f, 16f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.15f, 0.90f, 3.0f, 0.06f),
                mqE(2, 0.55f, 0.90f, 3.0f, 0.08f),
                mqL(3, 0.92f, 0.72f, 3.0f, 0.07f)
            ),
            obstacles = listOf(
                wall(0.24f, 0.36f, 0.06f, 0.08f),
                wall(0.44f, 0.58f, 0.06f, 0.08f),
                glass(0.64f, 0.36f, 0.06f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 35,
                maxMovableMirrors = 6,
                minReflections = 5,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.30f),
                    zone(0.20f, 0.42f, 0.50f, 0.52f)
                )
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ── Stage 50 – Volcano boss: 30 seconds, 8 mirrors, 4 mosquitoes ──────
        StageData(
            stageNumber = 50, worldNumber = 5,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.15f, 135f, 18f),
                mirror(2, 0.22f, 0.15f,  45f, 18f),
                mirror(3, 0.22f, 0.42f, 135f, 18f),
                mirror(4, 0.22f, 0.68f,  45f, 16f, movable = false),
                mirror(5, 0.5f,  0.42f,  45f, 18f),
                mirror(6, 0.5f,  0.68f, 135f, 16f, movable = false),
                mirror(7, 0.75f, 0.42f, 135f, 18f),
                mirror(8, 0.75f, 0.68f,  45f, 18f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.22f, 0.88f, 3.0f, 0.07f),
                mqE(2, 0.50f, 0.88f, 3.0f, 0.08f),
                mqL(3, 0.75f, 0.88f, 3.0f, 0.07f),
                mqE(4, 0.92f, 0.42f, 2.8f, 0.09f)
            ),
            obstacles = listOf(
                wall(0.34f, 0.28f, 0.06f, 0.08f),
                wall(0.62f, 0.28f, 0.06f, 0.08f),
                wall(0.34f, 0.52f, 0.06f, 0.08f),
                glass(0.62f, 0.52f, 0.06f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 30,
                maxMovableMirrors = 6,
                minReflections = 5,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.10f),
                    zone(0.30f, 0.30f, 0.70f, 0.38f)
                )
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ══════════════════════════════════════════════════════════════════════
        //  WORLD 6 – SPACE  (stages 51-60)
        //  Maximum complexity; stage 60 is the final boss.
        // ══════════════════════════════════════════════════════════════════════

        // ── Stage 51 – 45 seconds, 5 mirrors, 2 fast mosquitoes ──────────────
        StageData(
            stageNumber = 51, worldNumber = 6,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.20f, 0.5f,   45f, 20f),
                mirror(2, 0.20f, 0.75f, 135f, 20f),
                mirror(3, 0.50f, 0.75f,  45f, 20f),
                mirror(4, 0.50f, 0.5f,  135f, 20f),
                mirror(5, 0.75f, 0.5f,   45f, 20f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.20f, 0.92f, 3.0f, 0.07f),
                mqE(2, 0.92f, 0.5f,  3.0f, 0.09f)
            ),
            obstacles = listOf(
                wall(0.32f, 0.38f, 0.08f, 0.08f),
                wall(0.60f, 0.60f, 0.08f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 45,
                maxMovableMirrors = 4,
                minReflections = 4,
                forbiddenZones = listOf(zone(0.05f, 0.05f, 0.95f, 0.35f))
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 52 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 52, worldNumber = 6,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.18f,  45f, 18f),
                mirror(2, 0.78f, 0.18f, 135f, 18f),
                mirror(3, 0.78f, 0.48f,  45f, 18f),
                mirror(4, 0.5f,  0.48f, 135f, 18f),
                mirror(5, 0.25f, 0.48f,  45f, 18f),
                mirror(6, 0.25f, 0.75f, 135f, 16f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.78f, 0.78f, 3.0f, 0.07f),
                mqL(2, 0.50f, 0.85f, 3.0f, 0.08f),
                mqE(3, 0.10f, 0.75f, 2.8f, 0.09f)
            ),
            obstacles = listOf(
                wall(0.60f, 0.32f, 0.08f, 0.08f),
                wall(0.34f, 0.58f, 0.08f, 0.08f),
                glass(0.34f, 0.30f, 0.08f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 42,
                maxMovableMirrors = 5,
                minReflections = 4,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.45f, 0.14f),
                    zone(0.55f, 0.05f, 0.95f, 0.14f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 53 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 53, worldNumber = 6,
            laserSource = src(1f, 0.45f, 180f),
            mirrors = listOf(
                mirror(1, 0.80f, 0.45f, 135f, 18f),
                mirror(2, 0.80f, 0.70f,  45f, 18f),
                mirror(3, 0.58f, 0.70f, 135f, 18f),
                mirror(4, 0.58f, 0.45f,  45f, 18f),
                mirror(5, 0.36f, 0.45f, 135f, 18f),
                mirror(6, 0.36f, 0.70f,  45f, 18f),
                mirror(7, 0.15f, 0.70f, 135f, 16f, movable = false)
            ),
            mosquitoes = listOf(
                mqE(1, 0.80f, 0.88f, 3.0f, 0.08f),
                mqC(2, 0.58f, 0.88f, 3.0f, 0.07f),
                mqL(3, 0.08f, 0.70f, 3.0f, 0.08f)
            ),
            obstacles = listOf(
                wall(0.68f, 0.32f, 0.06f, 0.08f),
                wall(0.46f, 0.55f, 0.06f, 0.08f),
                wall(0.24f, 0.32f, 0.06f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 40,
                maxMovableMirrors = 6,
                minReflections = 5,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.32f),
                    zone(0.25f, 0.42f, 0.50f, 0.48f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 54 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 54, worldNumber = 6,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1,  0.14f, 0.5f,   45f, 16f),
                mirror(2,  0.14f, 0.72f, 135f, 16f),
                mirror(3,  0.32f, 0.72f,  45f, 16f),
                mirror(4,  0.32f, 0.5f,  135f, 16f),
                mirror(5,  0.50f, 0.5f,   45f, 16f),
                mirror(6,  0.50f, 0.72f, 135f, 16f),
                mirror(7,  0.68f, 0.72f,  45f, 16f),
                mirror(8,  0.68f, 0.5f,  135f, 14f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.14f, 0.90f, 3.0f, 0.06f),
                mqE(2, 0.50f, 0.90f, 3.0f, 0.08f),
                mqL(3, 0.88f, 0.5f,  3.0f, 0.07f)
            ),
            obstacles = listOf(
                wall(0.22f, 0.36f, 0.06f, 0.08f),
                wall(0.40f, 0.58f, 0.06f, 0.08f),
                wall(0.58f, 0.36f, 0.06f, 0.08f),
                glass(0.22f, 0.80f, 0.06f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 38,
                maxMovableMirrors = 7,
                minReflections = 6,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.35f),
                    zone(0.35f, 0.38f, 0.65f, 0.46f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 55 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 55, worldNumber = 6,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f,  0.14f, 135f, 15f),
                mirror(2, 0.25f, 0.14f,  45f, 15f),
                mirror(3, 0.25f, 0.38f, 135f, 15f),
                mirror(4, 0.25f, 0.62f,  45f, 15f),
                mirror(5, 0.5f,  0.38f,  45f, 15f),
                mirror(6, 0.5f,  0.62f, 135f, 14f, movable = false),
                mirror(7, 0.75f, 0.38f, 135f, 15f),
                mirror(8, 0.75f, 0.62f,  45f, 15f),
                mirror(9, 0.5f,  0.82f, 135f, 14f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.25f, 0.82f, 3.2f, 0.06f),
                mqE(2, 0.50f, 0.93f, 3.0f, 0.07f),
                mqL(3, 0.75f, 0.82f, 3.2f, 0.07f),
                mqE(4, 0.92f, 0.38f, 3.0f, 0.09f)
            ),
            obstacles = listOf(
                wall(0.35f, 0.25f, 0.07f, 0.08f),
                wall(0.60f, 0.25f, 0.07f, 0.08f),
                wall(0.35f, 0.48f, 0.07f, 0.08f),
                glass(0.60f, 0.48f, 0.07f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 35,
                maxMovableMirrors = 7,
                minReflections = 6,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.45f, 0.10f),
                    zone(0.55f, 0.05f, 0.95f, 0.10f),
                    zone(0.35f, 0.30f, 0.65f, 0.38f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 56 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 56, worldNumber = 6,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1,  0.10f, 0.5f,   45f, 14f),
                mirror(2,  0.10f, 0.70f, 135f, 14f),
                mirror(3,  0.28f, 0.70f,  45f, 14f),
                mirror(4,  0.28f, 0.5f,  135f, 14f),
                mirror(5,  0.46f, 0.5f,   45f, 14f),
                mirror(6,  0.46f, 0.70f, 135f, 14f),
                mirror(7,  0.64f, 0.70f,  45f, 14f),
                mirror(8,  0.64f, 0.5f,  135f, 14f),
                mirror(9,  0.82f, 0.5f,   45f, 14f),
                mirror(10, 0.82f, 0.70f, 135f, 12f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.10f, 0.88f, 3.2f, 0.06f),
                mqE(2, 0.46f, 0.88f, 3.0f, 0.07f),
                mqL(3, 0.82f, 0.88f, 3.2f, 0.06f),
                mqE(4, 0.94f, 0.5f,  3.0f, 0.08f)
            ),
            obstacles = listOf(
                wall(0.18f, 0.36f, 0.06f, 0.08f),
                wall(0.36f, 0.56f, 0.06f, 0.08f),
                wall(0.54f, 0.36f, 0.06f, 0.08f),
                wall(0.72f, 0.56f, 0.06f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 32,
                maxMovableMirrors = 9,
                minReflections = 7,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.35f),
                    zone(0.20f, 0.42f, 0.80f, 0.50f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 57 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 57, worldNumber = 6,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1,  0.5f,  0.12f, 135f, 14f),
                mirror(2,  0.22f, 0.12f,  45f, 14f),
                mirror(3,  0.22f, 0.35f, 135f, 14f),
                mirror(4,  0.22f, 0.58f,  45f, 14f),
                mirror(5,  0.22f, 0.78f, 135f, 12f, movable = false),
                mirror(6,  0.5f,  0.35f,  45f, 14f),
                mirror(7,  0.5f,  0.58f, 135f, 12f, movable = false),
                mirror(8,  0.75f, 0.35f, 135f, 14f),
                mirror(9,  0.75f, 0.58f,  45f, 14f),
                mirror(10, 0.75f, 0.78f, 135f, 14f),
                mirror(11, 0.5f,  0.78f,  45f, 14f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.22f, 0.93f, 3.2f, 0.06f),
                mqE(2, 0.5f,  0.93f, 3.0f, 0.07f),
                mqL(3, 0.75f, 0.93f, 3.2f, 0.06f),
                mqE(4, 0.08f, 0.58f, 3.0f, 0.08f),
                mqC(5, 0.92f, 0.35f, 3.0f, 0.07f)
            ),
            obstacles = listOf(
                wall(0.34f, 0.22f, 0.06f, 0.06f),
                wall(0.62f, 0.22f, 0.06f, 0.06f),
                wall(0.34f, 0.44f, 0.06f, 0.06f),
                glass(0.62f, 0.44f, 0.06f, 0.06f),
                glass(0.34f, 0.66f, 0.06f, 0.06f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 30,
                maxMovableMirrors = 9,
                minReflections = 8,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.08f),
                    zone(0.05f, 0.22f, 0.18f, 0.50f),
                    zone(0.82f, 0.22f, 0.95f, 0.50f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 58 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 58, worldNumber = 6,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1,  0.09f, 0.5f,   45f, 13f),
                mirror(2,  0.09f, 0.68f, 135f, 13f),
                mirror(3,  0.24f, 0.68f,  45f, 13f),
                mirror(4,  0.24f, 0.5f,  135f, 13f),
                mirror(5,  0.39f, 0.5f,   45f, 13f),
                mirror(6,  0.39f, 0.68f, 135f, 13f),
                mirror(7,  0.54f, 0.68f,  45f, 13f),
                mirror(8,  0.54f, 0.5f,  135f, 13f),
                mirror(9,  0.69f, 0.5f,   45f, 13f),
                mirror(10, 0.69f, 0.68f, 135f, 13f),
                mirror(11, 0.84f, 0.68f,  45f, 12f, movable = false),
                mirror(12, 0.84f, 0.5f,  135f, 12f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.09f, 0.86f, 3.2f, 0.06f),
                mqL(2, 0.39f, 0.86f, 3.2f, 0.06f),
                mqE(3, 0.69f, 0.86f, 3.0f, 0.07f),
                mqC(4, 0.94f, 0.68f, 3.2f, 0.06f),
                mqE(5, 0.94f, 0.5f,  3.0f, 0.08f)
            ),
            obstacles = listOf(
                wall(0.16f, 0.36f, 0.06f, 0.08f),
                wall(0.31f, 0.54f, 0.06f, 0.08f),
                wall(0.46f, 0.36f, 0.06f, 0.08f),
                wall(0.61f, 0.54f, 0.06f, 0.08f),
                glass(0.16f, 0.72f, 0.06f, 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 27,
                maxMovableMirrors = 10,
                minReflections = 9,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.35f),
                    zone(0.05f, 0.38f, 0.07f, 0.45f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 59 ──────────────────────────────────────────────────────────
        StageData(
            stageNumber = 59, worldNumber = 6,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1,  0.5f,  0.10f, 135f, 12f),
                mirror(2,  0.20f, 0.10f,  45f, 12f),
                mirror(3,  0.20f, 0.30f, 135f, 12f),
                mirror(4,  0.20f, 0.52f,  45f, 12f),
                mirror(5,  0.20f, 0.72f, 135f, 11f, movable = false),
                mirror(6,  0.40f, 0.30f,  45f, 12f),
                mirror(7,  0.40f, 0.52f, 135f, 12f),
                mirror(8,  0.40f, 0.72f,  45f, 11f, movable = false),
                mirror(9,  0.62f, 0.30f, 135f, 12f),
                mirror(10, 0.62f, 0.52f,  45f, 12f),
                mirror(11, 0.62f, 0.72f, 135f, 12f),
                mirror(12, 0.80f, 0.52f,  45f, 12f),
                mirror(13, 0.80f, 0.72f, 135f, 11f, movable = false)
            ),
            mosquitoes = listOf(
                mqC(1, 0.20f, 0.90f, 3.2f, 0.05f),
                mqE(2, 0.40f, 0.90f, 3.0f, 0.06f),
                mqL(3, 0.62f, 0.90f, 3.2f, 0.06f),
                mqC(4, 0.08f, 0.52f, 3.0f, 0.06f),
                mqE(5, 0.92f, 0.72f, 3.0f, 0.07f),
                mqL(6, 0.92f, 0.30f, 3.2f, 0.06f)
            ),
            obstacles = listOf(
                wall(0.30f, 0.18f, 0.06f, 0.07f),
                wall(0.52f, 0.18f, 0.06f, 0.07f),
                wall(0.30f, 0.40f, 0.06f, 0.07f),
                wall(0.52f, 0.40f, 0.06f, 0.07f),
                glass(0.30f, 0.60f, 0.06f, 0.07f),
                glass(0.52f, 0.60f, 0.06f, 0.07f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 23,
                maxMovableMirrors = 10,
                minReflections = 10,
                forbiddenZones = listOf(
                    zone(0.05f, 0.05f, 0.95f, 0.06f),
                    zone(0.05f, 0.18f, 0.15f, 0.78f),
                    zone(0.85f, 0.18f, 0.95f, 0.78f),
                    zone(0.30f, 0.26f, 0.70f, 0.34f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // ── Stage 60 – FINAL BOSS ─────────────────────────────────────────────
        // 20-second time limit. 15 mirrors form a snaking path from the bottom-
        // centre laser up through the whole arena, distributing the beam to cover
        // 20 mosquitoes across the upper two-thirds of the screen.
        StageData(
            stageNumber = 60, worldNumber = 6,
            laserSource = src(0.5f, 1f, 270f),            // shoots UP from bottom-centre
            mirrors = listOf(
                mirror(1,  0.50f, 0.80f,  45f, 10f),      // up→right
                mirror(2,  0.80f, 0.80f, 135f, 10f),      // right→up
                mirror(3,  0.80f, 0.60f,  45f, 10f),      // up→right (edge bounce)
                mirror(4,  0.20f, 0.60f, 135f, 10f),      // right→up (secondary)
                mirror(5,  0.20f, 0.42f,  45f, 10f),      // up→right
                mirror(6,  0.50f, 0.42f, 135f, 10f),      // right→up
                mirror(7,  0.65f, 0.42f,  45f, 10f),      // up→right
                mirror(8,  0.90f, 0.42f, 135f, 10f),      // right→up
                mirror(9,  0.90f, 0.28f,  45f, 10f),      // up→right (wall side)
                mirror(10, 0.10f, 0.28f, 135f, 10f),      // left side distributor
                mirror(11, 0.35f, 0.28f,  45f, 10f),
                mirror(12, 0.60f, 0.28f, 135f, 10f),
                mirror(13, 0.60f, 0.14f,  45f, 10f),
                mirror(14, 0.80f, 0.14f, 135f, 10f),
                mirror(15, 0.35f, 0.14f, 135f,  8f, movable = false)
            ),
            mosquitoes = listOf(
                // Top row (y≈0.08) – six erratic/circular
                mqE(1,  0.10f, 0.08f, 3.5f, 0.06f),
                mqE(2,  0.25f, 0.08f, 3.5f, 0.06f),
                mqC(3,  0.40f, 0.08f, 3.2f, 0.05f),
                mqE(4,  0.55f, 0.08f, 3.5f, 0.06f),
                mqC(5,  0.70f, 0.08f, 3.2f, 0.05f),
                mqE(6,  0.85f, 0.08f, 3.5f, 0.06f),
                // Second row (y≈0.18) – five mixed
                mqL(7,  0.10f, 0.18f, 3.4f, 0.06f),
                mqE(8,  0.30f, 0.18f, 3.3f, 0.07f),
                mqC(9,  0.50f, 0.18f, 3.2f, 0.06f),
                mqE(10, 0.70f, 0.18f, 3.3f, 0.07f),
                mqL(11, 0.90f, 0.18f, 3.4f, 0.06f),
                // Third row (y≈0.33) – five mixed
                mqE(12, 0.08f, 0.33f, 3.3f, 0.07f),
                mqC(13, 0.28f, 0.33f, 3.2f, 0.06f),
                mqE(14, 0.48f, 0.33f, 3.3f, 0.07f),
                mqC(15, 0.68f, 0.33f, 3.2f, 0.06f),
                mqE(16, 0.88f, 0.33f, 3.3f, 0.07f),
                // Lower scattered (y≈0.50) – four
                mqE(17, 0.15f, 0.50f, 3.2f, 0.08f),
                mqC(18, 0.40f, 0.50f, 3.0f, 0.07f),
                mqE(19, 0.65f, 0.50f, 3.2f, 0.08f),
                mqC(20, 0.90f, 0.50f, 3.0f, 0.07f)
            ),
            obstacles = listOf(
                wall(0.60f, 0.70f, 0.08f, 0.08f),
                wall(0.14f, 0.52f, 0.08f, 0.08f),
                wall(0.40f, 0.52f, 0.08f, 0.08f),
                glass(0.14f, 0.70f, 0.08f, 0.08f),
                glass(0.38f, 0.34f, 0.08f, 0.06f),
                glass(0.56f, 0.34f, 0.08f, 0.06f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 20,
                maxMovableMirrors = 14,
                minReflections = 10,
                forbiddenZones = listOf(
                    zone(0.05f, 0.60f, 0.45f, 0.68f),
                    zone(0.55f, 0.60f, 0.95f, 0.68f),
                    zone(0.35f, 0.85f, 0.65f, 1.00f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the [StageData] for the given 1-based [number].
     * @throws IndexOutOfBoundsException if [number] is not in 1..60.
     */
    fun getStage(number: Int): StageData = stages[number - 1]

    /**
     * Returns all ten stages that belong to the given [world] (1–6).
     */
    fun getWorldStages(world: Int): List<StageData> =
        stages.filter { it.worldNumber == world }

    /** Total number of stages in the game (always 60). */
    val stageCount: Int get() = stages.size
}
