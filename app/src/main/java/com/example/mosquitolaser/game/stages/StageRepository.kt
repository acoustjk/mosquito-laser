package com.example.mosquitolaser.game.stages

import android.graphics.RectF
import com.example.mosquitolaser.game.objects.LaserSource
import com.example.mosquitolaser.game.objects.Mirror
import com.example.mosquitolaser.game.objects.Mosquito
import com.example.mosquitolaser.game.objects.Obstacle

/**
 * Stage Repository containing all 60 stages across 6 themed worlds.
 *
 * 2D Reflection Table (Screen coords, Y-axis points DOWN):
 *   Formula: R = (2 * M - A) mod 360
 *
 *   45° Mirror (\ shape):
 *     - Right (0°)   -> DOWN (90°)
 *     - Down (90°)   -> RIGHT (0°)
 *     - Left (180°)  -> UP (270°)
 *     - Up (270°)    -> LEFT (180°)
 *
 *   135° Mirror (/ shape):
 *     - Right (0°)   -> UP (270°)
 *     - Up (270°)    -> RIGHT (0°)
 *     - Down (90°)   -> LEFT (180°)
 *     - Left (180°)  -> DOWN (90°)
 */
object StageRepository {

    // Helper builders for clean readability
    private fun src(x: Float, y: Float, angle: Float, color: Int = 0xFF00FFFF.toInt()) =
        LaserSource(x, y, angle, color)

    private fun mirror(id: Int, x: Float, y: Float, startAngle: Float, range: Float = 45f, movable: Boolean = true) =
        Mirror(id, x, y, startAngle, startAngle - range, startAngle + range, movable)

    private fun mq(id: Int, x: Float, y: Float) =
        Mosquito(id, x, y, Mosquito.MovementType.STATIC)

    private fun mqL(id: Int, x: Float, y: Float, speed: Float = 1.5f, range: Float = 0.1f) =
        Mosquito(id, x, y, Mosquito.MovementType.LINEAR, speed, range)

    private fun mqC(id: Int, x: Float, y: Float, speed: Float = 1.5f, range: Float = 0.08f) =
        Mosquito(id, x, y, Mosquito.MovementType.CIRCULAR, speed, range)

    private fun mqE(id: Int, x: Float, y: Float, speed: Float = 2.0f, range: Float = 0.12f) =
        Mosquito(id, x, y, Mosquito.MovementType.RANDOM, speed, range)

    private fun wall(x: Float, y: Float, w: Float, h: Float) =
        Obstacle(x, y, w, h, isSemiTransparent = false)

    private fun glass(x: Float, y: Float, w: Float, h: Float) =
        Obstacle(x, y, w, h, isSemiTransparent = true)

    private fun zone(l: Float, t: Float, r: Float, b: Float) =
        RectF(l, t, r, b)

    val stages: List<StageData> = listOf(

        // ======================================================================
        // WORLD 1 – NIGHT (stages 1-10)
        // Fundamentals: 1 to 4 mirrors, simple reflection paths, no obstacles.
        // ======================================================================

        // Stage 1: Tutorial 1 - Single mirror (right -> down)
        StageData(
            stageNumber = 1, worldNumber = 1,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(mirror(1, 0.5f, 0.4f, startAngle = 80f, range = 50f)), // sol=45°
            mosquitoes = listOf(mq(1, 0.5f, 0.85f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 2: Tutorial 2 - Single mirror (down -> right)
        StageData(
            stageNumber = 2, worldNumber = 1,
            laserSource = src(0.4f, 0f, 90f),
            mirrors = listOf(mirror(1, 0.4f, 0.4f, startAngle = 80f, range = 50f)), // sol=45°
            mosquitoes = listOf(mq(1, 0.82f, 0.4f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 3: Two mirrors (right -> down -> right)
        StageData(
            stageNumber = 3, worldNumber = 1,
            laserSource = src(0f, 0.25f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.25f, startAngle = 75f, range = 45f), // sol=45° (right->down)
                mirror(2, 0.35f, 0.65f, startAngle = 75f, range = 45f)  // sol=45° (down->right)
            ),
            mosquitoes = listOf(mq(1, 0.80f, 0.65f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 4: Two mirrors (down -> right -> down)
        StageData(
            stageNumber = 4, worldNumber = 1,
            laserSource = src(0.25f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.35f, startAngle = 75f, range = 50f), // sol=45° (down->right)
                mirror(2, 0.70f, 0.35f, startAngle = 75f, range = 50f)  // sol=45° (right->down)
            ),
            mosquitoes = listOf(mq(1, 0.70f, 0.85f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 5: Two mirrors chain (2 mosquitoes in beam path)
        StageData(
            stageNumber = 5, worldNumber = 1,
            laserSource = src(0.2f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.35f, startAngle = 75f, range = 50f), // sol=45° (down->right)
                mirror(2, 0.7f, 0.35f, startAngle = 75f, range = 50f)  // sol=45° (right->down)
            ),
            mosquitoes = listOf(
                mq(1, 0.45f, 0.35f), // mid-beam between M1 and M2
                mq(2, 0.7f, 0.75f)   // below M2
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 6: Three mirrors zigzag (right -> down -> right -> down)
        StageData(
            stageNumber = 6, worldNumber = 1,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.2f, startAngle = 75f, range = 50f),  // sol=45° (right->down)
                mirror(2, 0.3f, 0.55f, startAngle = 75f, range = 50f), // sol=45° (down->right)
                mirror(3, 0.7f, 0.55f, startAngle = 75f, range = 50f)  // sol=45° (right->down)
            ),
            mosquitoes = listOf(mq(1, 0.7f, 0.88f)),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 7: Three mirrors, 2 mosquitoes (down -> right -> down -> left)
        StageData(
            stageNumber = 7, worldNumber = 1,
            laserSource = src(0.25f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.35f, startAngle = 75f, range = 50f),  // sol=45° (down->right)
                mirror(2, 0.75f, 0.35f, startAngle = 75f, range = 50f),  // sol=45° (right->down)
                mirror(3, 0.75f, 0.72f, startAngle = 100f, range = 50f) // sol=135° (down->left)
            ),
            mosquitoes = listOf(
                mq(1, 0.50f, 0.35f), // in right-beam
                mq(2, 0.20f, 0.72f)  // in left-beam
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 8: Four mirrors chain
        StageData(
            stageNumber = 8, worldNumber = 1,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.15f, startAngle = 75f, range = 45f),  // sol=45° (right->down)
                mirror(2, 0.25f, 0.55f, startAngle = 75f, range = 45f),  // sol=45° (down->right)
                mirror(3, 0.65f, 0.55f, startAngle = 75f, range = 45f),  // sol=45° (right->down)
                mirror(4, 0.65f, 0.82f, startAngle = 100f, range = 45f)  // sol=135° (down->left)
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.35f),
                mq(2, 0.20f, 0.82f)
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 9: Four mirrors (left -> down -> left -> down)
        StageData(
            stageNumber = 9, worldNumber = 1,
            laserSource = src(1f, 0.2f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.2f, startAngle = 100f, range = 45f), // sol=135° (left->down)
                mirror(2, 0.75f, 0.55f, startAngle = 100f, range = 45f), // sol=135° (down->left)
                mirror(3, 0.35f, 0.55f, startAngle = 100f, range = 45f)  // sol=135° (left->down)
            ),
            mosquitoes = listOf(
                mq(1, 0.75f, 0.38f),
                mq(2, 0.35f, 0.82f)
            ),
            worldTheme = WorldTheme.NIGHT
        ),

        // Stage 10: World 1 Boss - Four mirrors, 3 mosquitoes, min 2 reflections
        StageData(
            stageNumber = 10, worldNumber = 1,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.15f, startAngle = 75f, range = 40f),  // sol=45°
                mirror(2, 0.25f, 0.50f, startAngle = 75f, range = 40f),  // sol=45°
                mirror(3, 0.65f, 0.50f, startAngle = 75f, range = 40f),  // sol=45°
                mirror(4, 0.65f, 0.80f, startAngle = 100f, range = 40f)  // sol=135°
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.32f),
                mq(2, 0.45f, 0.50f),
                mq(3, 0.20f, 0.80f)
            ),
            condition = StageCondition(minReflections = 2),
            worldTheme = WorldTheme.NIGHT
        ),

        // ======================================================================
        // WORLD 2 – CITY (stages 11-20)
        // Obstacles: Walls (solid blocks) and Glass (semi-transparent).
        // ======================================================================

        // Stage 11: Wall obstacle intro
        StageData(
            stageNumber = 11, worldNumber = 2,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(mirror(1, 0.65f, 0.3f, startAngle = 75f, range = 45f)), // sol=45° (right->down)
            mosquitoes = listOf(mq(1, 0.65f, 0.80f)),
            obstacles = listOf(wall(0.20f, 0.50f, 0.15f, 0.20f)), // Wall at bottom-left, out of beam
            worldTheme = WorldTheme.CITY
        ),

        // Stage 12: Bypassing a central wall
        StageData(
            stageNumber = 12, worldNumber = 2,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.4f, 0.2f, startAngle = 75f, range = 45f), // sol=45° (right->down)
                mirror(2, 0.4f, 0.7f, startAngle = 75f, range = 45f)  // sol=45° (down->right)
            ),
            mosquitoes = listOf(mq(1, 0.85f, 0.7f)),
            obstacles = listOf(wall(0.5f, 0.1f, 0.15f, 0.4f)), // Central wall blocking direct laser
            worldTheme = WorldTheme.CITY
        ),

        // Stage 13: Glass panel intro (laser passes through)
        StageData(
            stageNumber = 13, worldNumber = 2,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(
                mirror(1, 0.45f, 0.3f, startAngle = 75f, range = 45f), // sol=45°
                mirror(2, 0.45f, 0.7f, startAngle = 75f, range = 45f)  // sol=45°
            ),
            mosquitoes = listOf(mq(1, 0.85f, 0.7f)),
            obstacles = listOf(glass(0.60f, 0.60f, 0.10f, 0.20f)), // Glass panel in path
            worldTheme = WorldTheme.CITY
        ),

        // Stage 14: Two walls corridor
        StageData(
            stageNumber = 14, worldNumber = 2,
            laserSource = src(0.3f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.25f, startAngle = 75f, range = 45f),  // sol=45° (down->right)
                mirror(2, 0.75f, 0.25f, startAngle = 75f, range = 45f),  // sol=45° (right->down)
                mirror(3, 0.75f, 0.75f, startAngle = 100f, range = 45f)  // sol=135° (down->left)
            ),
            mosquitoes = listOf(
                mq(1, 0.75f, 0.50f),
                mq(2, 0.20f, 0.75f)
            ),
            obstacles = listOf(
                wall(0.45f, 0.05f, 0.10f, 0.18f),
                wall(0.45f, 0.35f, 0.10f, 0.30f)
            ),
            worldTheme = WorldTheme.CITY
        ),

        // Stage 15: Three mirrors around city blocks
        StageData(
            stageNumber = 15, worldNumber = 2,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.5f, startAngle = 75f, range = 45f),  // sol=45°
                mirror(2, 0.35f, 0.8f, startAngle = 75f, range = 45f),  // sol=45°
                mirror(3, 0.75f, 0.8f, startAngle = 75f, range = 45f)   // sol=45°
            ),
            mosquitoes = listOf(mq(1, 0.75f, 0.95f)),
            obstacles = listOf(
                wall(0.48f, 0.40f, 0.12f, 0.30f),
                wall(0.10f, 0.65f, 0.15f, 0.20f)
            ),
            worldTheme = WorldTheme.CITY
        ),

        // Stage 16: Four mirrors, 3 mosquitoes
        StageData(
            stageNumber = 16, worldNumber = 2,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.2f, startAngle = 75f, range = 45f),
                mirror(2, 0.3f, 0.55f, startAngle = 75f, range = 45f),
                mirror(3, 0.65f, 0.55f, startAngle = 75f, range = 45f),
                mirror(4, 0.65f, 0.80f, startAngle = 100f, range = 45f)
            ),
            mosquitoes = listOf(
                mq(1, 0.3f, 0.38f),
                mq(2, 0.48f, 0.55f),
                mq(3, 0.20f, 0.80f)
            ),
            obstacles = listOf(wall(0.45f, 0.10f, 0.10f, 0.35f)),
            worldTheme = WorldTheme.CITY
        ),

        // Stage 17: Multi-building maze
        StageData(
            stageNumber = 17, worldNumber = 2,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.5f, startAngle = 75f, range = 45f),
                mirror(2, 0.25f, 0.8f, startAngle = 75f, range = 45f),
                mirror(3, 0.55f, 0.8f, startAngle = 75f, range = 45f),
                mirror(4, 0.55f, 0.55f, startAngle = 100f, range = 45f),
                mirror(5, 0.85f, 0.55f, startAngle = 75f, range = 45f)
            ),
            mosquitoes = listOf(
                mq(1, 0.40f, 0.8f),
                mq(2, 0.85f, 0.82f)
            ),
            obstacles = listOf(
                wall(0.35f, 0.35f, 0.10f, 0.35f),
                wall(0.65f, 0.65f, 0.10f, 0.25f)
            ),
            worldTheme = WorldTheme.CITY
        ),

        // Stage 18: Glass & wall combination
        StageData(
            stageNumber = 18, worldNumber = 2,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.2f, startAngle = 75f, range = 45f),
                mirror(2, 0.25f, 0.6f, startAngle = 75f, range = 45f),
                mirror(3, 0.60f, 0.6f, startAngle = 75f, range = 45f),
                mirror(4, 0.60f, 0.35f, startAngle = 100f, range = 45f),
                mirror(5, 0.85f, 0.35f, startAngle = 75f, range = 45f)
            ),
            mosquitoes = listOf(mq(1, 0.85f, 0.75f)),
            obstacles = listOf(
                wall(0.38f, 0.10f, 0.10f, 0.40f),
                glass(0.70f, 0.30f, 0.08f, 0.40f)
            ),
            condition = StageCondition(minReflections = 3),
            worldTheme = WorldTheme.CITY
        ),

        // Stage 19: Precision city corridor
        StageData(
            stageNumber = 19, worldNumber = 2,
            laserSource = src(0.5f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.5f, 0.2f, startAngle = 75f, range = 40f),
                mirror(2, 0.8f, 0.2f, startAngle = 75f, range = 40f),
                mirror(3, 0.8f, 0.55f, startAngle = 75f, range = 40f),
                mirror(4, 0.5f, 0.55f, startAngle = 100f, range = 40f),
                mirror(5, 0.5f, 0.80f, startAngle = 75f, range = 40f),
                mirror(6, 0.2f, 0.80f, startAngle = 100f, range = 40f)
            ),
            mosquitoes = listOf(
                mq(1, 0.8f, 0.38f),
                mq(2, 0.5f, 0.68f),
                mq(3, 0.2f, 0.92f)
            ),
            obstacles = listOf(
                glass(0.60f, 0.30f, 0.10f, 0.15f),
                wall(0.30f, 0.60f, 0.12f, 0.15f)
            ),
            condition = StageCondition(minReflections = 3),
            worldTheme = WorldTheme.CITY
        ),

        // Stage 20: World 2 Boss - City Center Lockdown
        StageData(
            stageNumber = 20, worldNumber = 2,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.15f, startAngle = 75f, range = 35f),
                mirror(2, 0.2f, 0.50f, startAngle = 75f, range = 35f),
                mirror(3, 0.5f, 0.50f, startAngle = 75f, range = 35f),
                mirror(4, 0.5f, 0.80f, startAngle = 100f, range = 35f),
                mirror(5, 0.8f, 0.80f, startAngle = 75f, range = 35f),
                mirror(6, 0.8f, 0.50f, startAngle = 100f, range = 35f)
            ),
            mosquitoes = listOf(
                mq(1, 0.2f, 0.32f),
                mq(2, 0.35f, 0.50f),
                mq(3, 0.8f, 0.93f)
            ),
            obstacles = listOf(
                wall(0.32f, 0.25f, 0.10f, 0.18f),
                wall(0.62f, 0.55f, 0.10f, 0.18f)
            ),
            condition = StageCondition(minReflections = 4),
            worldTheme = WorldTheme.CITY
        ),

        // ======================================================================
        // WORLD 3 – JUNGLE (stages 21-30)
        // Mechanics: Moving mosquitoes (Linear, Circular, Random) + Mirror limits.
        // ======================================================================

        // Stage 21: Linear mosquito intro
        StageData(
            stageNumber = 21, worldNumber = 3,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(mirror(1, 0.5f, 0.4f, startAngle = 75f, range = 50f)), // sol=45°
            mosquitoes = listOf(mqL(1, 0.5f, 0.75f, speed = 1.2f, range = 0.12f)),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 22: Linear mosquito timing
        StageData(
            stageNumber = 22, worldNumber = 3,
            laserSource = src(0f, 0.25f, 0f),
            mirrors = listOf(
                mirror(1, 0.4f, 0.25f, startAngle = 75f, range = 45f),
                mirror(2, 0.4f, 0.65f, startAngle = 75f, range = 45f)
            ),
            mosquitoes = listOf(mqL(1, 0.75f, 0.65f, speed = 1.4f, range = 0.10f)),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 23: Two linear mosquitoes
        StageData(
            stageNumber = 23, worldNumber = 3,
            laserSource = src(0.3f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.3f, startAngle = 75f, range = 45f),
                mirror(2, 0.7f, 0.3f, startAngle = 75f, range = 45f),
                mirror(3, 0.7f, 0.7f, startAngle = 100f, range = 45f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.5f, 0.3f, speed = 1.5f, range = 0.08f),
                mqL(2, 0.35f, 0.7f, speed = 1.3f, range = 0.10f)
            ),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 24: Mirror Move Limit (Max 2 mirrors)
        StageData(
            stageNumber = 24, worldNumber = 3,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.3f, startAngle = 75f, range = 45f),
                mirror(2, 0.35f, 0.7f, startAngle = 75f, range = 45f),
                mirror(3, 0.75f, 0.7f, startAngle = 45f, range = 0f, movable = false) // Fixed mirror
            ),
            mosquitoes = listOf(mqL(1, 0.75f, 0.88f, speed = 1.5f, range = 0.08f)),
            condition = StageCondition(maxMovableMirrors = 2),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 25: Circular mosquito intro
        StageData(
            stageNumber = 25, worldNumber = 3,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.4f, 0.4f, startAngle = 75f, range = 45f),
                mirror(2, 0.4f, 0.75f, startAngle = 75f, range = 45f)
            ),
            mosquitoes = listOf(mqC(1, 0.75f, 0.75f, speed = 1.6f, range = 0.07f)),
            condition = StageCondition(maxMovableMirrors = 2),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 26: Static + Linear combination
        StageData(
            stageNumber = 26, worldNumber = 3,
            laserSource = src(0.25f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.3f, startAngle = 75f, range = 45f),
                mirror(2, 0.65f, 0.3f, startAngle = 75f, range = 45f),
                mirror(3, 0.65f, 0.65f, startAngle = 100f, range = 45f)
            ),
            mosquitoes = listOf(
                mq(1, 0.45f, 0.3f),
                mqL(2, 0.35f, 0.65f, speed = 1.4f, range = 0.09f)
            ),
            condition = StageCondition(maxMovableMirrors = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 27: Two circular mosquitoes with obstacles
        StageData(
            stageNumber = 27, worldNumber = 3,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.2f, startAngle = 75f, range = 40f),
                mirror(2, 0.3f, 0.6f, startAngle = 75f, range = 40f),
                mirror(3, 0.7f, 0.6f, startAngle = 75f, range = 40f),
                mirror(4, 0.7f, 0.85f, startAngle = 100f, range = 40f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.5f, 0.6f, speed = 1.8f, range = 0.06f),
                mqC(2, 0.35f, 0.85f, speed = 1.8f, range = 0.06f)
            ),
            obstacles = listOf(wall(0.45f, 0.25f, 0.10f, 0.25f)),
            condition = StageCondition(maxMovableMirrors = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 28: Mixed movement (Linear + Circular + Static)
        StageData(
            stageNumber = 28, worldNumber = 3,
            laserSource = src(1f, 0.3f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.3f, startAngle = 100f, range = 40f),
                mirror(2, 0.75f, 0.65f, startAngle = 75f, range = 40f),
                mirror(3, 0.35f, 0.65f, startAngle = 100f, range = 40f),
                mirror(4, 0.35f, 0.85f, startAngle = 75f, range = 0f, movable = false)
            ),
            mosquitoes = listOf(
                mqL(1, 0.55f, 0.65f, speed = 1.4f, range = 0.08f),
                mqC(2, 0.55f, 0.85f, speed = 1.6f, range = 0.07f),
                mq(3, 0.75f, 0.48f)
            ),
            condition = StageCondition(maxMovableMirrors = 3, minReflections = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 29: Erratic (Random) mosquito intro
        StageData(
            stageNumber = 29, worldNumber = 3,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.4f, startAngle = 75f, range = 40f),
                mirror(2, 0.35f, 0.75f, startAngle = 75f, range = 40f),
                mirror(3, 0.75f, 0.75f, startAngle = 75f, range = 40f)
            ),
            mosquitoes = listOf(
                mqE(1, 0.75f, 0.88f, speed = 2.0f, range = 0.08f),
                mq(2, 0.55f, 0.75f)
            ),
            obstacles = listOf(glass(0.48f, 0.60f, 0.10f, 0.20f)),
            condition = StageCondition(maxMovableMirrors = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // Stage 30: World 3 Boss - Jungle Swarm
        StageData(
            stageNumber = 30, worldNumber = 3,
            laserSource = src(0.25f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.25f, startAngle = 75f, range = 35f),
                mirror(2, 0.65f, 0.25f, startAngle = 75f, range = 35f),
                mirror(3, 0.65f, 0.55f, startAngle = 100f, range = 35f),
                mirror(4, 0.25f, 0.55f, startAngle = 75f, range = 0f, movable = false),
                mirror(5, 0.25f, 0.82f, startAngle = 75f, range = 35f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.45f, 0.25f, speed = 1.6f, range = 0.08f),
                mqC(2, 0.45f, 0.55f, speed = 1.8f, range = 0.07f),
                mqE(3, 0.55f, 0.82f, speed = 2.0f, range = 0.09f)
            ),
            obstacles = listOf(wall(0.40f, 0.35f, 0.10f, 0.15f)),
            condition = StageCondition(maxMovableMirrors = 4, minReflections = 3),
            worldTheme = WorldTheme.JUNGLE
        ),

        // ======================================================================
        // WORLD 4 – FACTORY (stages 31-40)
        // Mechanics: Time limits (30-60s) + Forbidden zones.
        // ======================================================================

        // Stage 31: Time limit intro (60s)
        StageData(
            stageNumber = 31, worldNumber = 4,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.4f, 0.4f, startAngle = 75f, range = 45f),
                mirror(2, 0.4f, 0.75f, startAngle = 75f, range = 45f)
            ),
            mosquitoes = listOf(mq(1, 0.8f, 0.75f)),
            condition = StageCondition(timeLimitSeconds = 60),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 32: 55s timer, 2 mosquitoes
        StageData(
            stageNumber = 32, worldNumber = 4,
            laserSource = src(0f, 0.25f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.25f, startAngle = 75f, range = 45f),
                mirror(2, 0.35f, 0.65f, startAngle = 75f, range = 45f),
                mirror(3, 0.75f, 0.65f, startAngle = 75f, range = 45f)
            ),
            mosquitoes = listOf(
                mq(1, 0.35f, 0.45f),
                mq(2, 0.75f, 0.88f)
            ),
            condition = StageCondition(timeLimitSeconds = 55),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 33: 50s timer, moving mosquito
        StageData(
            stageNumber = 33, worldNumber = 4,
            laserSource = src(0.4f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.4f, 0.3f, startAngle = 75f, range = 40f),
                mirror(2, 0.8f, 0.3f, startAngle = 75f, range = 40f),
                mirror(3, 0.8f, 0.7f, startAngle = 100f, range = 40f)
            ),
            mosquitoes = listOf(mqL(1, 0.4f, 0.7f, speed = 1.5f, range = 0.10f)),
            condition = StageCondition(timeLimitSeconds = 50),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 34: Forbidden Zone intro
        StageData(
            stageNumber = 34, worldNumber = 4,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.5f, startAngle = 75f, range = 45f),
                mirror(2, 0.3f, 0.8f, startAngle = 75f, range = 45f),
                mirror(3, 0.7f, 0.8f, startAngle = 75f, range = 45f)
            ),
            mosquitoes = listOf(mq(1, 0.7f, 0.95f)),
            condition = StageCondition(
                timeLimitSeconds = 50,
                forbiddenZones = listOf(zone(0.4f, 0.1f, 0.9f, 0.45f)) // Top-right hazard zone
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 35: Forbidden Zone + 2 Mirrors
        StageData(
            stageNumber = 35, worldNumber = 4,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.2f, startAngle = 75f, range = 40f),
                mirror(2, 0.35f, 0.7f, startAngle = 75f, range = 40f),
                mirror(3, 0.75f, 0.7f, startAngle = 75f, range = 40f)
            ),
            mosquitoes = listOf(
                mq(1, 0.35f, 0.45f),
                mqC(2, 0.75f, 0.88f, speed = 1.6f, range = 0.07f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 45,
                forbiddenZones = listOf(zone(0.45f, 0.10f, 0.90f, 0.45f))
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 36: 45s timer, 5 mirrors, factory machinery
        StageData(
            stageNumber = 36, worldNumber = 4,
            laserSource = src(1f, 0.25f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.25f, startAngle = 100f, range = 35f),
                mirror(2, 0.75f, 0.6f, startAngle = 75f, range = 35f),
                mirror(3, 0.35f, 0.6f, startAngle = 100f, range = 35f),
                mirror(4, 0.35f, 0.85f, startAngle = 75f, range = 35f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.55f, 0.6f, speed = 1.6f, range = 0.08f),
                mqC(2, 0.6f, 0.85f, speed = 1.8f, range = 0.07f)
            ),
            obstacles = listOf(wall(0.45f, 0.10f, 0.15f, 0.35f)),
            condition = StageCondition(timeLimitSeconds = 45),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 37: 40s timer, tight angles + hazard zone
        StageData(
            stageNumber = 37, worldNumber = 4,
            laserSource = src(0.3f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.25f, startAngle = 75f, range = 35f),
                mirror(2, 0.75f, 0.25f, startAngle = 75f, range = 35f),
                mirror(3, 0.75f, 0.65f, startAngle = 100f, range = 35f),
                mirror(4, 0.3f, 0.65f, startAngle = 75f, range = 35f)
            ),
            mosquitoes = listOf(
                mq(1, 0.52f, 0.25f),
                mqL(2, 0.3f, 0.88f, speed = 1.8f, range = 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 40,
                forbiddenZones = listOf(zone(0.40f, 0.35f, 0.65f, 0.55f))
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 38: Dual hazard zones
        StageData(
            stageNumber = 38, worldNumber = 4,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.4f, startAngle = 75f, range = 35f),
                mirror(2, 0.25f, 0.75f, startAngle = 75f, range = 35f),
                mirror(3, 0.65f, 0.75f, startAngle = 75f, range = 35f),
                mirror(4, 0.65f, 0.4f, startAngle = 100f, range = 35f)
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.58f),
                mqE(2, 0.85f, 0.4f, speed = 2.0f, range = 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 40,
                forbiddenZones = listOf(
                    zone(0.35f, 0.10f, 0.55f, 0.60f),
                    zone(0.75f, 0.55f, 0.95f, 0.90f)
                )
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 39: High speed assembly line
        StageData(
            stageNumber = 39, worldNumber = 4,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.2f, startAngle = 75f, range = 30f),
                mirror(2, 0.3f, 0.5f, startAngle = 75f, range = 30f),
                mirror(3, 0.7f, 0.5f, startAngle = 75f, range = 30f),
                mirror(4, 0.7f, 0.8f, startAngle = 100f, range = 30f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.5f, 0.5f, speed = 2.0f, range = 0.08f),
                mqC(2, 0.4f, 0.8f, speed = 2.0f, range = 0.07f)
            ),
            obstacles = listOf(wall(0.42f, 0.05f, 0.10f, 0.35f)),
            condition = StageCondition(
                timeLimitSeconds = 35,
                maxMovableMirrors = 3
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // Stage 40: World 4 Boss - Meltdown Alert (30s, 3 Mosquitoes, Hazard Zone)
        StageData(
            stageNumber = 40, worldNumber = 4,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.15f, startAngle = 75f, range = 30f),
                mirror(2, 0.25f, 0.50f, startAngle = 75f, range = 30f),
                mirror(3, 0.65f, 0.50f, startAngle = 75f, range = 30f),
                mirror(4, 0.65f, 0.80f, startAngle = 100f, range = 30f),
                mirror(5, 0.30f, 0.80f, startAngle = 75f, range = 30f)
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.32f),
                mqL(2, 0.45f, 0.50f, speed = 1.8f, range = 0.08f),
                mqE(3, 0.30f, 0.92f, speed = 2.2f, range = 0.08f)
            ),
            obstacles = listOf(wall(0.40f, 0.20f, 0.10f, 0.20f)),
            condition = StageCondition(
                timeLimitSeconds = 30,
                maxMovableMirrors = 4,
                minReflections = 3,
                forbiddenZones = listOf(zone(0.75f, 0.10f, 0.95f, 0.60f))
            ),
            worldTheme = WorldTheme.FACTORY
        ),

        // ======================================================================
        // WORLD 5 – VOLCANO (stages 41-50)
        // Mechanics: Fast erratic enemies, tight angle ranges (15-25°), multi-conditions.
        // ======================================================================

        // Stage 41: Fast circular mosquito + tight angle
        StageData(
            stageNumber = 41, worldNumber = 5,
            laserSource = src(0f, 0.35f, 0f),
            mirrors = listOf(
                mirror(1, 0.45f, 0.35f, startAngle = 65f, range = 25f),
                mirror(2, 0.45f, 0.72f, startAngle = 65f, range = 25f)
            ),
            mosquitoes = listOf(mqC(1, 0.80f, 0.72f, speed = 2.4f, range = 0.07f)),
            condition = StageCondition(timeLimitSeconds = 45, minReflections = 2),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 42: Two fast mosquitoes, 4 mirrors
        StageData(
            stageNumber = 42, worldNumber = 5,
            laserSource = src(0.3f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.25f, startAngle = 65f, range = 25f),
                mirror(2, 0.7f, 0.25f, startAngle = 65f, range = 25f),
                mirror(3, 0.7f, 0.65f, startAngle = 115f, range = 25f),
                mirror(4, 0.3f, 0.65f, startAngle = 65f, range = 25f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.5f, 0.25f, speed = 2.5f, range = 0.06f),
                mqL(2, 0.3f, 0.85f, speed = 2.2f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 45, maxMovableMirrors = 3),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 43: Magma chamber corridor
        StageData(
            stageNumber = 43, worldNumber = 5,
            laserSource = src(1f, 0.3f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.3f, startAngle = 115f, range = 25f),
                mirror(2, 0.75f, 0.7f, startAngle = 65f, range = 25f),
                mirror(3, 0.4f, 0.7f, startAngle = 115f, range = 25f),
                mirror(4, 0.4f, 0.4f, startAngle = 65f, range = 0f, movable = false),
                mirror(5, 0.15f, 0.4f, startAngle = 115f, range = 25f)
            ),
            mosquitoes = listOf(
                mqE(1, 0.15f, 0.7f, speed = 2.4f, range = 0.08f),
                mqC(2, 0.58f, 0.7f, speed = 2.2f, range = 0.07f)
            ),
            obstacles = listOf(wall(0.55f, 0.10f, 0.10f, 0.45f)),
            condition = StageCondition(timeLimitSeconds = 40, minReflections = 3),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 44: Lava pit hazard
        StageData(
            stageNumber = 44, worldNumber = 5,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.5f, startAngle = 65f, range = 25f),
                mirror(2, 0.25f, 0.8f, startAngle = 65f, range = 25f),
                mirror(3, 0.65f, 0.8f, startAngle = 65f, range = 25f),
                mirror(4, 0.65f, 0.5f, startAngle = 115f, range = 25f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.45f, 0.8f, speed = 2.6f, range = 0.06f),
                mqL(2, 0.85f, 0.5f, speed = 2.4f, range = 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 38,
                forbiddenZones = listOf(zone(0.35f, 0.10f, 0.85f, 0.40f))
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 45: 5 Mirrors tight puzzle
        StageData(
            stageNumber = 45, worldNumber = 5,
            laserSource = src(0.2f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.3f, startAngle = 65f, range = 20f),
                mirror(2, 0.6f, 0.3f, startAngle = 65f, range = 20f),
                mirror(3, 0.6f, 0.65f, startAngle = 115f, range = 20f),
                mirror(4, 0.2f, 0.65f, startAngle = 65f, range = 20f),
                mirror(5, 0.2f, 0.88f, startAngle = 65f, range = 20f)
            ),
            mosquitoes = listOf(
                mqE(1, 0.4f, 0.3f, speed = 2.5f, range = 0.08f),
                mqC(2, 0.6f, 0.88f, speed = 2.5f, range = 0.07f)
            ),
            condition = StageCondition(timeLimitSeconds = 35, maxMovableMirrors = 4),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 46: Triple hazard zone
        StageData(
            stageNumber = 46, worldNumber = 5,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.2f, startAngle = 65f, range = 20f),
                mirror(2, 0.3f, 0.55f, startAngle = 65f, range = 20f),
                mirror(3, 0.7f, 0.55f, startAngle = 65f, range = 20f),
                mirror(4, 0.7f, 0.80f, startAngle = 115f, range = 20f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.5f, 0.55f, speed = 2.6f, range = 0.08f),
                mqE(2, 0.4f, 0.80f, speed = 2.6f, range = 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 35,
                forbiddenZones = listOf(
                    zone(0.40f, 0.05f, 0.90f, 0.45f),
                    zone(0.05f, 0.65f, 0.20f, 0.95f)
                )
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 47: 6 mirrors corridor
        StageData(
            stageNumber = 47, worldNumber = 5,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.15f, startAngle = 65f, range = 20f),
                mirror(2, 0.2f, 0.45f, startAngle = 65f, range = 20f),
                mirror(3, 0.5f, 0.45f, startAngle = 65f, range = 20f),
                mirror(4, 0.5f, 0.75f, startAngle = 115f, range = 20f),
                mirror(5, 0.8f, 0.75f, startAngle = 65f, range = 20f),
                mirror(6, 0.8f, 0.45f, startAngle = 115f, range = 20f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.35f, 0.45f, speed = 2.5f, range = 0.06f),
                mqE(2, 0.65f, 0.75f, speed = 2.6f, range = 0.08f),
                mqL(3, 0.8f, 0.20f, speed = 2.5f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 32, maxMovableMirrors = 5),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 48: Extreme speed swarm
        StageData(
            stageNumber = 48, worldNumber = 5,
            laserSource = src(1f, 0.2f, 180f),
            mirrors = listOf(
                mirror(1, 0.75f, 0.2f, startAngle = 115f, range = 20f),
                mirror(2, 0.75f, 0.5f, startAngle = 65f, range = 20f),
                mirror(3, 0.4f, 0.5f, startAngle = 115f, range = 20f),
                mirror(4, 0.4f, 0.8f, startAngle = 65f, range = 20f),
                mirror(5, 0.15f, 0.8f, startAngle = 115f, range = 20f)
            ),
            mosquitoes = listOf(
                mqE(1, 0.58f, 0.5f, speed = 2.8f, range = 0.08f),
                mqC(2, 0.28f, 0.8f, speed = 2.8f, range = 0.07f),
                mqL(3, 0.15f, 0.5f, speed = 2.6f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 30, minReflections = 4),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 49: Volcano Core (7 mirrors, 3 fast enemies)
        StageData(
            stageNumber = 49, worldNumber = 5,
            laserSource = src(0.2f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.2f, startAngle = 65f, range = 18f),
                mirror(2, 0.5f, 0.2f, startAngle = 65f, range = 18f),
                mirror(3, 0.5f, 0.5f, startAngle = 115f, range = 18f),
                mirror(4, 0.8f, 0.5f, startAngle = 65f, range = 18f),
                mirror(5, 0.8f, 0.8f, startAngle = 65f, range = 18f),
                mirror(6, 0.2f, 0.8f, startAngle = 115f, range = 18f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.35f, 0.2f, speed = 2.8f, range = 0.06f),
                mqE(2, 0.65f, 0.5f, speed = 2.8f, range = 0.08f),
                mqL(3, 0.50f, 0.8f, speed = 2.8f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 28, maxMovableMirrors = 5),
            worldTheme = WorldTheme.VOLCANO
        ),

        // Stage 50: World 5 Boss - Volcano Eruption (25s, 4 fast enemies, min 4 reflections)
        StageData(
            stageNumber = 50, worldNumber = 5,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.15f, startAngle = 65f, range = 18f),
                mirror(2, 0.25f, 0.45f, startAngle = 65f, range = 18f),
                mirror(3, 0.55f, 0.45f, startAngle = 65f, range = 18f),
                mirror(4, 0.55f, 0.75f, startAngle = 115f, range = 18f),
                mirror(5, 0.85f, 0.75f, startAngle = 65f, range = 18f),
                mirror(6, 0.85f, 0.45f, startAngle = 115f, range = 18f)
            ),
            mosquitoes = listOf(
                mq(1, 0.25f, 0.30f),
                mqC(2, 0.40f, 0.45f, speed = 3.0f, range = 0.06f),
                mqE(3, 0.70f, 0.75f, speed = 3.0f, range = 0.08f),
                mqL(4, 0.85f, 0.20f, speed = 2.8f, range = 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 25,
                maxMovableMirrors = 5,
                minReflections = 4,
                forbiddenZones = listOf(zone(0.35f, 0.10f, 0.75f, 0.35f))
            ),
            worldTheme = WorldTheme.VOLCANO
        ),

        // ======================================================================
        // WORLD 6 – SPACE (stages 51-60)
        // Cosmic void, extreme multi-bounce labyrinths, fast swarms.
        // ======================================================================

        // Stage 51: Space Void intro
        StageData(
            stageNumber = 51, worldNumber = 6,
            laserSource = src(0f, 0.3f, 0f),
            mirrors = listOf(
                mirror(1, 0.35f, 0.3f, startAngle = 65f, range = 20f),
                mirror(2, 0.35f, 0.7f, startAngle = 65f, range = 20f),
                mirror(3, 0.75f, 0.7f, startAngle = 65f, range = 20f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.55f, 0.7f, speed = 2.8f, range = 0.06f),
                mqE(2, 0.75f, 0.88f, speed = 2.8f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 40),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 52: Cosmic ray corridor
        StageData(
            stageNumber = 52, worldNumber = 6,
            laserSource = src(0.3f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.25f, startAngle = 65f, range = 20f),
                mirror(2, 0.7f, 0.25f, startAngle = 65f, range = 20f),
                mirror(3, 0.7f, 0.6f, startAngle = 115f, range = 20f),
                mirror(4, 0.3f, 0.6f, startAngle = 65f, range = 20f)
            ),
            mosquitoes = listOf(
                mqL(1, 0.5f, 0.25f, speed = 2.8f, range = 0.08f),
                mqC(2, 0.5f, 0.6f, speed = 2.8f, range = 0.06f),
                mqE(3, 0.3f, 0.85f, speed = 2.8f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 35, minReflections = 3),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 53: Nebula maze (5 mirrors)
        StageData(
            stageNumber = 53, worldNumber = 6,
            laserSource = src(0f, 0.2f, 0f),
            mirrors = listOf(
                mirror(1, 0.25f, 0.2f, startAngle = 65f, range = 18f),
                mirror(2, 0.25f, 0.5f, startAngle = 65f, range = 18f),
                mirror(3, 0.6f, 0.5f, startAngle = 65f, range = 18f),
                mirror(4, 0.6f, 0.8f, startAngle = 115f, range = 18f),
                mirror(5, 0.85f, 0.8f, startAngle = 65f, range = 18f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.42f, 0.5f, speed = 2.8f, range = 0.06f),
                mqE(2, 0.72f, 0.8f, speed = 3.0f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 35, maxMovableMirrors = 4),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 54: Black hole hazard zone
        StageData(
            stageNumber = 54, worldNumber = 6,
            laserSource = src(0f, 0.4f, 0f),
            mirrors = listOf(
                mirror(1, 0.3f, 0.4f, startAngle = 65f, range = 18f),
                mirror(2, 0.3f, 0.8f, startAngle = 65f, range = 18f),
                mirror(3, 0.7f, 0.8f, startAngle = 65f, range = 18f),
                mirror(4, 0.7f, 0.4f, startAngle = 115f, range = 18f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.5f, 0.8f, speed = 3.0f, range = 0.06f),
                mqE(2, 0.85f, 0.4f, speed = 3.0f, range = 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 32,
                forbiddenZones = listOf(zone(0.40f, 0.10f, 0.85f, 0.55f)) // Black hole singularity
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 55: 6 Mirrors space station grid
        StageData(
            stageNumber = 55, worldNumber = 6,
            laserSource = src(0.2f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.2f, startAngle = 65f, range = 16f),
                mirror(2, 0.5f, 0.2f, startAngle = 65f, range = 16f),
                mirror(3, 0.5f, 0.5f, startAngle = 115f, range = 16f),
                mirror(4, 0.8f, 0.5f, startAngle = 65f, range = 16f),
                mirror(5, 0.8f, 0.8f, startAngle = 65f, range = 16f),
                mirror(6, 0.2f, 0.8f, startAngle = 115f, range = 16f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.35f, 0.2f, speed = 3.0f, range = 0.06f),
                mqE(2, 0.65f, 0.5f, speed = 3.0f, range = 0.08f),
                mqL(3, 0.50f, 0.8f, speed = 3.0f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 30, minReflections = 4),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 56: Galaxy core (7 mirrors, 3 erratic swarmers)
        StageData(
            stageNumber = 56, worldNumber = 6,
            laserSource = src(0f, 0.15f, 0f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.15f, startAngle = 65f, range = 16f),
                mirror(2, 0.2f, 0.45f, startAngle = 65f, range = 16f),
                mirror(3, 0.5f, 0.45f, startAngle = 65f, range = 16f),
                mirror(4, 0.5f, 0.75f, startAngle = 115f, range = 16f),
                mirror(5, 0.8f, 0.75f, startAngle = 65f, range = 16f),
                mirror(6, 0.8f, 0.45f, startAngle = 115f, range = 16f),
                mirror(7, 0.5f, 0.15f, startAngle = 65f, range = 16f)
            ),
            mosquitoes = listOf(
                mqE(1, 0.35f, 0.45f, speed = 3.0f, range = 0.08f),
                mqC(2, 0.65f, 0.75f, speed = 3.0f, range = 0.06f),
                mqE(3, 0.80f, 0.25f, speed = 3.0f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 28, maxMovableMirrors = 6),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 57: Quantum rift (Dual black holes)
        StageData(
            stageNumber = 57, worldNumber = 6,
            laserSource = src(0f, 0.5f, 0f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.5f, startAngle = 65f, range = 15f),
                mirror(2, 0.2f, 0.8f, startAngle = 65f, range = 15f),
                mirror(3, 0.5f, 0.8f, startAngle = 65f, range = 15f),
                mirror(4, 0.5f, 0.5f, startAngle = 115f, range = 15f),
                mirror(5, 0.8f, 0.5f, startAngle = 65f, range = 15f),
                mirror(6, 0.8f, 0.8f, startAngle = 115f, range = 15f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.35f, 0.8f, speed = 3.2f, range = 0.06f),
                mqE(2, 0.65f, 0.5f, speed = 3.2f, range = 0.08f),
                mqL(3, 0.8f, 0.93f, speed = 3.0f, range = 0.08f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 25,
                forbiddenZones = listOf(
                    zone(0.30f, 0.10f, 0.70f, 0.35f),
                    zone(0.05f, 0.60f, 0.15f, 0.95f)
                )
            ),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 58: Hyper-speed orbital laser
        StageData(
            stageNumber = 58, worldNumber = 6,
            laserSource = src(1f, 0.2f, 180f),
            mirrors = listOf(
                mirror(1, 0.8f, 0.2f, startAngle = 115f, range = 15f),
                mirror(2, 0.8f, 0.5f, startAngle = 65f, range = 15f),
                mirror(3, 0.5f, 0.5f, startAngle = 115f, range = 15f),
                mirror(4, 0.5f, 0.8f, startAngle = 65f, range = 15f),
                mirror(5, 0.2f, 0.8f, startAngle = 115f, range = 15f),
                mirror(6, 0.2f, 0.5f, startAngle = 65f, range = 15f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.65f, 0.5f, speed = 3.2f, range = 0.06f),
                mqE(2, 0.35f, 0.8f, speed = 3.2f, range = 0.08f),
                mqL(3, 0.2f, 0.3f, speed = 3.0f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 25, minReflections = 5),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 59: Cosmic Labyrinth (8 mirrors, 4 mosquitoes)
        StageData(
            stageNumber = 59, worldNumber = 6,
            laserSource = src(0.2f, 0f, 90f),
            mirrors = listOf(
                mirror(1, 0.2f, 0.2f, startAngle = 65f, range = 15f),
                mirror(2, 0.5f, 0.2f, startAngle = 65f, range = 15f),
                mirror(3, 0.5f, 0.45f, startAngle = 115f, range = 15f),
                mirror(4, 0.8f, 0.45f, startAngle = 65f, range = 15f),
                mirror(5, 0.8f, 0.7f, startAngle = 65f, range = 15f),
                mirror(6, 0.5f, 0.7f, startAngle = 115f, range = 15f),
                mirror(7, 0.5f, 0.9f, startAngle = 65f, range = 15f),
                mirror(8, 0.2f, 0.9f, startAngle = 115f, range = 15f)
            ),
            mosquitoes = listOf(
                mqC(1, 0.35f, 0.2f, speed = 3.2f, range = 0.06f),
                mqE(2, 0.65f, 0.45f, speed = 3.2f, range = 0.08f),
                mqL(3, 0.65f, 0.7f, speed = 3.0f, range = 0.08f),
                mqE(4, 0.35f, 0.9f, speed = 3.2f, range = 0.08f)
            ),
            condition = StageCondition(timeLimitSeconds = 22, maxMovableMirrors = 6),
            worldTheme = WorldTheme.SPACE
        ),

        // Stage 60: GRAND FINAL BOSS - Galactic Mosquito Overlord
        // Laser fires UP from bottom-center src(0.5f, 1f, 270f).
        // 8 movable mirrors distribute laser beam across cosmic arena to destroy 5 boss swarmers.
        StageData(
            stageNumber = 60, worldNumber = 6,
            laserSource = src(0.5f, 1f, 270f), // Shoots straight UP (270°)
            mirrors = listOf(
                mirror(1, 0.5f, 0.75f, startAngle = 65f, range = 15f),  // sol=45° (up -> left)
                mirror(2, 0.2f, 0.75f, startAngle = 115f, range = 15f), // sol=135° (left -> up)
                mirror(3, 0.2f, 0.45f, startAngle = 65f, range = 15f),  // sol=45° (up -> left)
                mirror(4, 0.8f, 0.45f, startAngle = 115f, range = 15f), // sol=135° (left -> down/up)
                mirror(5, 0.8f, 0.2f, startAngle = 65f, range = 15f),   // sol=45° (up -> left)
                mirror(6, 0.5f, 0.2f, startAngle = 115f, range = 15f),  // sol=135° (left -> down)
                mirror(7, 0.5f, 0.45f, startAngle = 65f, range = 15f),  // sol=45°
                mirror(8, 0.2f, 0.2f, startAngle = 65f, range = 15f)    // sol=45°
            ),
            mosquitoes = listOf(
                mqE(1, 0.35f, 0.75f, speed = 3.5f, range = 0.06f),
                mqC(2, 0.2f, 0.60f, speed = 3.2f, range = 0.05f),
                mqE(3, 0.5f, 0.45f, speed = 3.5f, range = 0.06f),
                mqC(4, 0.65f, 0.2f, speed = 3.2f, range = 0.05f),
                mqE(5, 0.35f, 0.2f, speed = 3.5f, range = 0.06f)
            ),
            obstacles = listOf(
                glass(0.40f, 0.30f, 0.20f, 0.10f),
                wall(0.35f, 0.60f, 0.10f, 0.10f)
            ),
            condition = StageCondition(
                timeLimitSeconds = 25,
                maxMovableMirrors = 8,
                minReflections = 5
            ),
            worldTheme = WorldTheme.SPACE
        )
    )

    fun getStage(number: Int): StageData = stages[number - 1]

    fun getWorldStages(world: Int): List<StageData> =
        stages.filter { it.worldNumber == world }

    val stageCount: Int get() = stages.size
}
