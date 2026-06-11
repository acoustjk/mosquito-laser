package com.example.mosquitolaser.game

import com.example.mosquitolaser.game.objects.Mirror
import com.example.mosquitolaser.game.objects.Mosquito
import com.example.mosquitolaser.game.objects.Obstacle
import com.example.mosquitolaser.game.stages.StageData
import kotlin.math.*

/**
 * Laser ray-casting and reflection calculator.
 * All coordinates are normalized (0.0 to 1.0).
 */
object LaserCalculator {

    const val MAX_BOUNCES = 20

    data class LaserSegment(
        val x1: Float, val y1: Float,
        val x2: Float, val y2: Float,
        val bounceIndex: Int
    )

    data class LaserResult(
        val segments: List<LaserSegment>,
        val hitMosquitoes: Set<Int>,
        val reflectionCount: Int,
        val hitForbiddenZone: Boolean
    )

    fun calculate(
        stage: StageData,
        liveMosquitoes: List<Mosquito>,
        liveMirrors: List<Mirror>,
        canvasW: Float,
        canvasH: Float
    ): LaserResult {
        val segments = mutableListOf<LaserSegment>()
        val hitMosquitoes = mutableSetOf<Int>()
        var reflectionCount = 0
        var hitForbiddenZone = false

        var currentX = stage.laserSource.x
        var currentY = stage.laserSource.y
        var currentAngle = stage.laserSource.angle

        for (bounce in 0..MAX_BOUNCES) {
            val result = castRay(currentX, currentY, currentAngle, liveMirrors, liveMosquitoes, stage.obstacles)

            val endX = result.x
            val endY = result.y

            segments.add(LaserSegment(currentX, currentY, endX, endY, bounce))

            // Check forbidden zones
            if (stage.condition.hasForbiddenZones()) {
                for (zone in stage.condition.forbiddenZones) {
                    if (segmentIntersectsRect(currentX, currentY, endX, endY,
                            zone.left, zone.top, zone.right, zone.bottom)) {
                        hitForbiddenZone = true
                    }
                }
            }

            // Check mosquito hits along this segment
            for (mosquito in liveMosquitoes) {
                if (mosquito.isAlive && !hitMosquitoes.contains(mosquito.id)) {
                    if (segmentHitsMosquito(currentX, currentY, endX, endY, mosquito)) {
                        hitMosquitoes.add(mosquito.id)
                    }
                }
            }

            when (result.hitType) {
                HitType.BOUNDARY, HitType.OBSTACLE -> break
                HitType.MIRROR -> {
                    val mirror = result.mirror ?: break
                    currentAngle = mirror.reflect(currentAngle)
                    currentX = result.x
                    currentY = result.y
                    reflectionCount++
                }
                HitType.SEMI_OBSTACLE -> {
                    currentX = result.x
                    currentY = result.y
                }
            }
        }

        return LaserResult(segments, hitMosquitoes, reflectionCount, hitForbiddenZone)
    }

    private enum class HitType { BOUNDARY, OBSTACLE, MIRROR, SEMI_OBSTACLE }

    private data class RayHit(
        val x: Float, val y: Float,
        val distance: Float,
        val hitType: HitType,
        val mirror: Mirror? = null
    )

    private fun castRay(
        startX: Float, startY: Float, angleDeg: Float,
        mirrors: List<Mirror>, mosquitoes: List<Mosquito>, obstacles: List<Obstacle>
    ): RayHit {
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val dx = cos(angleRad).toFloat()
        val dy = sin(angleRad).toFloat()

        var nearestHit = rayBoundaryIntersection(startX, startY, dx, dy)

        for (mirror in mirrors) {
            val hit = rayMirrorIntersection(startX, startY, dx, dy, mirror)
            if (hit != null && hit.distance > 0.002f && hit.distance < nearestHit.distance) {
                nearestHit = hit
            }
        }

        for (obstacle in obstacles) {
            val hit = rayObstacleIntersection(startX, startY, dx, dy, obstacle)
            if (hit != null && hit.distance > 0.002f && hit.distance < nearestHit.distance) {
                nearestHit = hit
            }
        }

        return nearestHit
    }

    private fun rayBoundaryIntersection(x: Float, y: Float, dx: Float, dy: Float): RayHit {
        var tMin = Float.MAX_VALUE
        if (dx < -1e-6f) { val t = -x / dx; if (t > 0.002f && t < tMin) tMin = t }
        if (dx > 1e-6f)  { val t = (1f - x) / dx; if (t > 0.002f && t < tMin) tMin = t }
        if (dy < -1e-6f) { val t = -y / dy; if (t > 0.002f && t < tMin) tMin = t }
        if (dy > 1e-6f)  { val t = (1f - y) / dy; if (t > 0.002f && t < tMin) tMin = t }
        if (tMin == Float.MAX_VALUE) tMin = 2f
        return RayHit(x + dx * tMin, y + dy * tMin, tMin, HitType.BOUNDARY)
    }

    private fun rayMirrorIntersection(
        rx: Float, ry: Float, rdx: Float, rdy: Float, mirror: Mirror
    ): RayHit? {
        val mirrorAngleRad = Math.toRadians(mirror.angle.toDouble())
        val halfLen = mirror.size / 2f
        val cos = cos(mirrorAngleRad).toFloat()
        val sin = sin(mirrorAngleRad).toFloat()

        val x1 = mirror.x - cos * halfLen
        val y1 = mirror.y - sin * halfLen
        val x2 = mirror.x + cos * halfLen
        val y2 = mirror.y + sin * halfLen

        val t = raySegmentIntersection(rx, ry, rdx, rdy, x1, y1, x2, y2) ?: return null
        if (t < 0.002f) return null

        return RayHit(rx + rdx * t, ry + rdy * t, t, HitType.MIRROR, mirror)
    }

    private fun rayObstacleIntersection(
        rx: Float, ry: Float, rdx: Float, rdy: Float, obstacle: Obstacle
    ): RayHit? {
        val left = obstacle.x; val top = obstacle.y
        val right = obstacle.x + obstacle.width; val bottom = obstacle.y + obstacle.height
        var tMin = 0f; var tMax = Float.MAX_VALUE

        if (abs(rdx) < 1e-6f) {
            if (rx < left || rx > right) return null
        } else {
            var t1 = (left - rx) / rdx; var t2 = (right - rx) / rdx
            if (t1 > t2) { val tmp = t1; t1 = t2; t2 = tmp }
            tMin = max(tMin, t1); tMax = min(tMax, t2)
        }

        if (abs(rdy) < 1e-6f) {
            if (ry < top || ry > bottom) return null
        } else {
            var t1 = (top - ry) / rdy; var t2 = (bottom - ry) / rdy
            if (t1 > t2) { val tmp = t1; t1 = t2; t2 = tmp }
            tMin = max(tMin, t1); tMax = min(tMax, t2)
        }

        if (tMax < tMin || tMin < 0.002f) return null

        val hitType = if (obstacle.isSemiTransparent) HitType.SEMI_OBSTACLE else HitType.OBSTACLE
        return RayHit(rx + rdx * tMin, ry + rdy * tMin, tMin, hitType)
    }

    private fun raySegmentIntersection(
        rx: Float, ry: Float, rdx: Float, rdy: Float,
        x1: Float, y1: Float, x2: Float, y2: Float
    ): Float? {
        val sdx = x2 - x1; val sdy = y2 - y1
        val denom = rdx * sdy - rdy * sdx
        if (abs(denom) < 1e-6f) return null

        val t = ((x1 - rx) * sdy - (y1 - ry) * sdx) / denom
        val u = ((x1 - rx) * rdy - (y1 - ry) * rdx) / denom
        if (t < 0f || u < 0f || u > 1f) return null
        return t
    }

    private fun segmentHitsMosquito(
        x1: Float, y1: Float, x2: Float, y2: Float, mosquito: Mosquito
    ): Boolean {
        val dx = x2 - x1; val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq < 1e-10f) return false
        val t = ((mosquito.x - x1) * dx + (mosquito.y - y1) * dy) / lenSq
        val clampedT = t.coerceIn(0f, 1f)
        val closestX = x1 + clampedT * dx; val closestY = y1 + clampedT * dy
        val dist = hypot(closestX - mosquito.x, closestY - mosquito.y)
        return dist <= mosquito.hitRadius
    }

    private fun segmentIntersectsRect(
        x1: Float, y1: Float, x2: Float, y2: Float,
        rLeft: Float, rTop: Float, rRight: Float, rBottom: Float
    ): Boolean {
        fun code(x: Float, y: Float): Int {
            var c = 0
            if (x < rLeft) c = c or 1; if (x > rRight) c = c or 2
            if (y < rTop) c = c or 4; if (y > rBottom) c = c or 8
            return c
        }
        val c1 = code(x1, y1); val c2 = code(x2, y2)
        if (c1 and c2 != 0) return false
        return true
    }
}
