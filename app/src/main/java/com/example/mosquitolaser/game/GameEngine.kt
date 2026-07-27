package com.example.mosquitolaser.game

import com.example.mosquitolaser.game.objects.Mirror
import com.example.mosquitolaser.game.objects.Mosquito
import com.example.mosquitolaser.game.stages.StageData
import kotlin.math.hypot

/**
 * Central game state manager.
 * Handles game logic, state transitions, input processing, and win/lose conditions.
 */
class GameEngine(val stage: StageData) {

    enum class GameState { PLAYING, PAUSED, STAGE_CLEAR, STAGE_FAIL }
    enum class FailReason { TIME_UP, FORBIDDEN_ZONE }

    var state: GameState = GameState.PLAYING
        private set

    // Live copies of game objects
    val mosquitoes: MutableList<Mosquito> = stage.mosquitoes.map {
        Mosquito(it.id, it.x, it.y, it.movementType, it.moveSpeed, it.moveRange, it.movePhase, it.isAlive, it.hitRadius)
    }.toMutableList()

    val mirrors: MutableList<Mirror> = stage.mirrors.map {
        Mirror(it.id, it.x, it.y, it.angle, it.minAngle, it.maxAngle, it.isMovable, it.size)
    }.toMutableList()

    // Timer
    var remainingTimeMs: Long = if (stage.condition.hasTimeLimit())
        stage.condition.timeLimitSeconds * 1000L else Long.MAX_VALUE
        private set

    var elapsedTimeMs: Long = 0L
        private set

    // Score
    var reflectionCount: Int = 0
        private set
    var mosquitoesKilled: Int = 0
        private set
    var mirrorsMoved: Int = 0
        private set

    private val movedMirrorIds = mutableSetOf<Int>()
    var selectedMirrorId: Int = -1
    var laserResult: LaserCalculator.LaserResult? = null
        private set
    var canvasW: Float = 1f
    var canvasH: Float = 1f

    // Callbacks
    var onMosquitoKilled: ((Mosquito) -> Unit)? = null
    var onStageClear: (() -> Unit)? = null
    var onStageFail: ((reason: FailReason) -> Unit)? = null

    private var hitForbiddenZone = false
    private var clearTriggered = false
    private var failTriggered = false

    fun update(deltaMs: Long) {
        if (state != GameState.PLAYING) return
        elapsedTimeMs += deltaMs

        // Update timer
        if (stage.condition.hasTimeLimit()) {
            remainingTimeMs -= deltaMs
            if (remainingTimeMs <= 0) {
                remainingTimeMs = 0
                triggerFail(FailReason.TIME_UP)
                return
            }
        }

        // Update mosquito positions
        mosquitoes.forEach { if (it.isAlive) it.update(deltaMs) }

        // Recalculate laser
        recalculateLaser()
    }

    // ── Score & Star Calculation System ─────────────────────────────────────

    val baseStageScore: Int
        get() = stage.mosquitoes.size * 1000 + stage.mirrors.size * 300

    val speedBonus: Int
        get() = if (stage.condition.hasTimeLimit()) {
            ((remainingTimeMs / 1000f) * 150).toInt().coerceAtLeast(0)
        } else {
            (3500 - (elapsedTimeMs / 1000f) * 100).toInt().coerceAtLeast(0)
        }

    val movableMirrorsCount: Int
        get() = stage.mirrors.count { it.isMovable }

    val unusedMirrorsCount: Int
        get() = (movableMirrorsCount - mirrorsMoved).coerceAtLeast(0)

    val burnSpeedMultiplier: Float
        get() = when {
            reflectionCount >= 3 -> 3.5f
            reflectionCount == 2 -> 2.0f // 2 mirrors used = 2x burn speed!
            else -> 1.0f
        }

    val reflectionBonus: Int
        get() = when {
            stage.mirrors.size > 1 && reflectionCount >= stage.mirrors.size -> 2500 // All mirrors used bonus!
            reflectionCount >= 3 -> 3500
            reflectionCount == 2 -> 1500
            reflectionCount == 1 -> 300
            else -> 0
        }

    val efficiencyBonus: Int
        get() {
            val moveEfficiencyScore = unusedMirrorsCount * 500
            return moveEfficiencyScore + reflectionBonus
        }

    val totalScore: Int
        get() = baseStageScore + speedBonus + efficiencyBonus

    fun calculateStars(): Int {
        val total = totalScore
        val target3Star = baseStageScore + 1200
        val target2Star = baseStageScore + 400
        val usedAllMirrors = stage.mirrors.size > 1 && reflectionCount >= stage.mirrors.size

        return when {
            usedAllMirrors || total >= target3Star || (unusedMirrorsCount > 0 && elapsedTimeMs <= 25_000L) -> 3
            total >= target2Star || mirrorsMoved <= movableMirrorsCount -> 2
            else -> 1
        }
    }

    private fun recalculateLaser() {
        val result = LaserCalculator.calculate(stage, mosquitoes, mirrors, canvasW, canvasH)
        laserResult = result
        reflectionCount = result.reflectionCount
        hitForbiddenZone = result.hitForbiddenZone

        // Process laser burn with multi-mirror power acceleration
        val hitIds = result.hitMosquitoes
        val mult = burnSpeedMultiplier

        for (mosquito in mosquitoes) {
            if (!mosquito.isAlive) {
                mosquito.isBeingHitByLaser = false
                continue
            }

            val isHit = hitIds.contains(mosquito.id)
            mosquito.isBeingHitByLaser = isHit

            if (isHit) {
                // Laser burn rate scales with reflection count (2 mirrors = 2x speed -> 1.5s burn!)
                val burnStep = (16L * mult).toLong()
                mosquito.burnTimeMs += burnStep
                if (mosquito.burnTimeMs >= mosquito.requiredBurnTimeMs) {
                    mosquito.isAlive = false
                    mosquitoesKilled++
                    onMosquitoKilled?.invoke(mosquito)
                }
            } else {
                // Cool down slowly when laser leaves the mosquito
                if (mosquito.burnTimeMs > 0L) {
                    mosquito.burnTimeMs = (mosquito.burnTimeMs - 8L).coerceAtLeast(0L)
                }
            }
        }

        checkWinCondition()
    }

    private fun checkWinCondition() {
        if (state != GameState.PLAYING || clearTriggered || failTriggered) return

        // ALL mosquitoes must be dead to clear the stage!
        val allDead = mosquitoes.isNotEmpty() && mosquitoes.all { !it.isAlive }
        if (!allDead) return

        if (stage.condition.hasMinReflections() && reflectionCount < stage.condition.minReflections) return

        if (hitForbiddenZone) {
            triggerFail(FailReason.FORBIDDEN_ZONE)
            return
        }

        triggerClear()
    }

    private fun triggerClear() {
        clearTriggered = true
        state = GameState.STAGE_CLEAR
        onStageClear?.invoke()
    }

    private fun triggerFail(reason: FailReason) {
        failTriggered = true
        state = GameState.STAGE_FAIL
        onStageFail?.invoke(reason)
    }

    fun onTouchDown(normX: Float, normY: Float): Boolean {
        if (state != GameState.PLAYING) return false

        for (mirror in mirrors) {
            if (!mirror.isMovable) continue

            if (stage.condition.hasMirrorLimit()) {
                val isNewMirror = !movedMirrorIds.contains(mirror.id)
                if (isNewMirror && movedMirrorIds.size >= stage.condition.maxMovableMirrors) continue
            }

            val dist = hypot((normX - mirror.x).toDouble(), (normY - mirror.y).toDouble()).toFloat()
            if (dist < mirror.size + 0.08f) {
                // Toggle: tap same mirror → deselect, tap different mirror → select
                selectedMirrorId = if (selectedMirrorId == mirror.id) -1 else mirror.id
                return selectedMirrorId != -1
            }
        }
        // Tap on empty space → deselect
        selectedMirrorId = -1
        return false
    }

    /** Called by the angle dial when the user rotates it. */
    fun onDialAngleChanged(angleDeg: Float) {
        if (state != GameState.PLAYING) return
        val mirror = mirrors.find { it.id == selectedMirrorId } ?: return
        mirror.angle = angleDeg.coerceIn(mirror.minAngle, mirror.maxAngle)
        movedMirrorIds.add(mirror.id)
        mirrorsMoved = movedMirrorIds.size
        recalculateLaser()
    }

    // onTouchMove is no longer used for mirror rotation (dial handles it)
    fun onTouchMove(normX: Float, normY: Float) { /* no-op – dial controls rotation */ }

    fun onTouchUp() { /* selection persists until user taps elsewhere */ }

    fun pause() { if (state == GameState.PLAYING) state = GameState.PAUSED }
    fun resume() { if (state == GameState.PAUSED) state = GameState.PLAYING }

    fun reset() {
        state = GameState.PLAYING
        clearTriggered = false
        failTriggered = false
        mosquitoes.clear()
        mosquitoes.addAll(stage.mosquitoes.map {
            Mosquito(it.id, it.x, it.y, it.movementType, it.moveSpeed, it.moveRange, it.movePhase, true, it.hitRadius)
        })
        mirrors.clear()
        mirrors.addAll(stage.mirrors.map {
            Mirror(it.id, it.x, it.y, it.angle, it.minAngle, it.maxAngle, it.isMovable, it.size)
        })
        remainingTimeMs = if (stage.condition.hasTimeLimit())
            stage.condition.timeLimitSeconds * 1000L else Long.MAX_VALUE
        reflectionCount = 0
        mosquitoesKilled = 0
        mirrorsMoved = 0
        movedMirrorIds.clear()
        selectedMirrorId = -1
        laserResult = null
        hitForbiddenZone = false
    }

    val totalMosquitoes: Int get() = mosquitoes.size
    val remainingSeconds: Int get() = (remainingTimeMs / 1000L).toInt()
    val isConditionMet: Boolean get() {
        if (hitForbiddenZone) return false
        if (stage.condition.hasMinReflections() && reflectionCount < stage.condition.minReflections) return false
        return true
    }
}
