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

    private fun recalculateLaser() {
        val result = LaserCalculator.calculate(stage, mosquitoes, mirrors, canvasW, canvasH)
        laserResult = result
        reflectionCount = result.reflectionCount
        hitForbiddenZone = result.hitForbiddenZone

        // Process mosquito hits
        for (id in result.hitMosquitoes) {
            val mosquito = mosquitoes.find { it.id == id }
            if (mosquito != null && mosquito.isAlive) {
                mosquito.isAlive = false
                mosquitoesKilled++
                onMosquitoKilled?.invoke(mosquito)
            }
        }

        checkWinCondition()
    }

    private fun checkWinCondition() {
        if (state != GameState.PLAYING || clearTriggered || failTriggered) return

        val allDead = mosquitoes.all { !it.isAlive }
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
            if (dist < mirror.size + 0.06f) {
                selectedMirrorId = mirror.id
                return true
            }
        }
        selectedMirrorId = -1
        return false
    }

    fun onTouchMove(normX: Float, normY: Float) {
        if (state != GameState.PLAYING) return
        val mirror = mirrors.find { it.id == selectedMirrorId } ?: return

        val dx = normX - mirror.x
        val dy = normY - mirror.y
        val angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
        mirror.angle = angle.coerceIn(mirror.minAngle, mirror.maxAngle)

        movedMirrorIds.add(mirror.id)
        mirrorsMoved = movedMirrorIds.size

        recalculateLaser()
    }

    fun onTouchUp() { selectedMirrorId = -1 }

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
