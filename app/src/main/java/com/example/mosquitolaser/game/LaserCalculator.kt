package com.example.mosquitolaser.game

import com.example.mosquitolaser.game.objects.Mirror
import com.example.mosquitolaser.game.objects.Mosquito
import com.example.mosquitolaser.game.objects.Obstacle
import com.example.mosquitolaser.game.stages.StageData
import kotlin.math.*

/**
 * Laser ray-casting and reflection calculator.
 * All internal geometry calculations are performed in Pixel Space
 * using canvasW and canvasH to guarantee 100% physically accurate 1:1 angles.
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

        // Guard against zero / unitialized canvas dimensions
        val w = if (canvasW > 1f) canvasW else 1000f
        val h = if (canvasH > 1f) canvasH else 1000f

        var currentPx = stage.laserSource.x * w
        var currentPy = stage.laserSource.y * h
        var currentAngle = stage.laserSource.angle

        for (bounce in 0..MAX_BOUNCES) {
            val result = castRayPixel(currentPx, currentPy, currentAngle, liveMirrors, liveMosquitoes, stage.obstacles, w, h)

            val endPx = result.px
            val endPy = result.py

            // Store normalized segment coordinates for drawing/rendering consistency
            segments.add(LaserSegment(currentPx / w, currentPy / h, endPx / w, endPy / h, bounce))

            // Check forbidden zones (in normalized space)
            val segNormX1 = currentPx / w; val segNormY1 = currentPy / h
            val segNormX2 = endPx / w; val segNormY2 = endPy / h
            if (stage.condition.hasForbiddenZones()) {
                for (zone in stage.condition.forbiddenZones) {
                    if (segmentIntersectsRect(segNormX1, segNormY1, segNormX2, segNormY2,
                            zone.left, zone.top, zone.right, zone.bottom)) {
                        hitForbiddenZone = true
                    }
                }
            }

            // Check mosquito hits along this segment in pixel space
            for (mosquito in liveMosquitoes) {
                if (mosquito.isAlive && !hitMosquitoes.contains(mosquito.id)) {
                    if (segmentHitsMosquitoPixel(currentPx, currentPy, endPx, endPy, mosquito, w, h)) {
                        hitMosquitoes.add(mosquito.id)
                    }
                }
            }

            when (result.hitType) {
                HitType.BOUNDARY, HitType.OBSTACLE -> break
                HitType.MIRROR -> {
                    val mirror = result.mirror ?: break
                    currentAngle = mirror.reflect(currentAngle)
                    currentPx = result.px
                    currentPy = result.py
                    reflectionCount++
                }
                HitType.SEMI_OBSTACLE -> {
                    currentPx = result.px
                    currentPy = result.py
                }
            }
        }

        return LaserResult(segments, hitMosquitoes, reflectionCount, hitForbiddenZone)
    }

    private enum class HitType { BOUNDARY, OBSTACLE, MIRROR, SEMI_OBSTACLE }

    private data class RayHit(
        val px: Float, val py: Float,
        val distance: Float,
        val hitType: HitType,
        val mirror: Mirror? = null
    )

    private fun castRayPixel(
        startPx: Float, startPy: Float, angleDeg: Float,
        mirrors: List<Mirror>, mosquitoes: List<Mosquito>, obstacles: List<Obstacle>,
        w: Float, h: Float
    ): RayHit {
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val rdx = cos(angleRad).toFloat()
        val rdy = sin(angleRad).toFloat()

        var nearestHit = rayBoundaryIntersectionPixel(startPx, startPy, rdx, rdy, w, h)

        for (mirror in mirrors) {
            val hit = rayMirrorIntersectionPixel(startPx, startPy, rdx, rdy, mirror, w, h)
            if (hit != null && hit.distance > 1f && hit.distance < nearestHit.distance) {
                nearestHit = hit
            }
        }

        for (obstacle in obstacles) {
            val hit = rayObstacleIntersectionPixel(startPx, startPy, rdx, rdy, obstacle, w, h)
            if (hit != null && hit.distance > 1f && hit.distance < nearestHit.distance) {
                nearestHit = hit
            }
        }

        return nearestHit
    }

    private fun rayBoundaryIntersectionPixel(
        px: Float, py: Float, rdx: Float, rdy: Float, w: Float, h: Float
    ): RayHit {
        var tMin = Float.MAX_VALUE
        if (rdx < -1e-6f) { val t = -px / rdx; if (t > 1f && t < tMin) tMin = t }
        if (rdx > 1e-6f)  { val t = (w - px) / rdx; if (t > 1f && t < tMin) tMin = t }
        if (rdy < -1e-6f) { val t = -py / rdy; if (t > 1f && t < tMin) tMin = t }
        if (rdy > 1e-6f)  { val t = (h - py) / rdy; if (t > 1f && t < tMin) tMin = t }
        if (tMin == Float.MAX_VALUE) tMin = max(w, h) * 2f
        return RayHit(px + rdx * tMin, py + rdy * tMin, tMin, HitType.BOUNDARY)
    }

    private fun rayMirrorIntersectionPixel(
        rx: Float, ry: Float, rdx: Float, rdy: Float, mirror: Mirror, w: Float, h: Float
    ): RayHit? {
        val mx = mirror.x * w
        val my = mirror.y * h
        val mirrorAngleRad = Math.toRadians(mirror.angle.toDouble())
        val halfLen = mirror.size * min(w, h) / 2f
        val cos = cos(mirrorAngleRad).toFloat()
        val sin = sin(mirrorAngleRad).toFloat()

        val x1 = mx - cos * halfLen
        val y1 = my - sin * halfLen
        val x2 = mx + cos * halfLen
        val y2 = my + sin * halfLen

        val t = raySegmentIntersection(rx, ry, rdx, rdy, x1, y1, x2, y2) ?: return null
        if (t < 1.0f) return null

        return RayHit(rx + rdx * t, ry + rdy * t, t, HitType.MIRROR, mirror)
    }

    private fun rayObstacleIntersectionPixel(
        rx: Float, ry: Float, rdx: Float, rdy: Float, obstacle: Obstacle, w: Float, h: Float
    ): RayHit? {
        val left = obstacle.x * w; val top = obstacle.y * h
        val right = left + obstacle.width * w; val bottom = top + obstacle.height * h
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

        if (tMax < tMin || tMin < 1.0f) return null

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

    private fun segmentHitsMosquitoPixel(
        x1: Float, y1: Float, x2: Float, y2: Float, mosquito: Mosquito, w: Float, h: Float
    ): Boolean {
        val mqx = mosquito.x * w
        val mqy = mosquito.y * h
        val hitRadiusPx = mosquito.hitRadius * min(w, h)

        val dx = x2 - x1; val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq < 1e-6f) return false
        val t = ((mqx - x1) * dx + (mqy - y1) * dy) / lenSq
        val clampedT = t.coerceIn(0f, 1f)
        val closestX = x1 + clampedT * dx; val closestY = y1 + clampedT * dy
        val dist = hypot(closestX - mqx, closestY - mqy)
        return dist <= hitRadiusPx
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
