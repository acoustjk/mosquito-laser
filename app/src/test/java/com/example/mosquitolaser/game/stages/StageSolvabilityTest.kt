package com.example.mosquitolaser.game.stages

import com.example.mosquitolaser.game.LaserCalculator
import org.junit.Assert.*
import org.junit.Test

class StageSolvabilityTest {

    @Test
    fun testAll60StagesSolvable() {
        val totalStages = StageRepository.stageCount
        assertEquals(60, totalStages)

        val unsolvableStages = mutableListOf<Int>()

        for (stageNum in 1..60) {
            val stage = StageRepository.getStage(stageNum)
            var solvable = false

            // Test grid of mirror angle steps to find a solution
            // Movable mirrors can take angles in [minAngle, maxAngle]
            val movableMirrors = stage.mirrors.filter { it.isMovable }

            // Test with discrete steps (e.g. 5 degree steps for solution verification)
            fun testMirrorAngles(index: Int): Boolean {
                if (index >= movableMirrors.size) {
                    val res = LaserCalculator.calculate(stage, stage.mosquitoes, stage.mirrors, 1080f, 2400f)
                    val allMosquitoesHit = res.hitMosquitoes.size == stage.mosquitoes.size
                    val reflectionsOk = res.reflectionCount >= stage.condition.minReflections
                    val noForbiddenZone = !res.hitForbiddenZone
                    return allMosquitoesHit && reflectionsOk && noForbiddenZone
                }

                val mirror = movableMirrors[index]
                val origAngle = mirror.angle
                val step = 5f
                var angle = mirror.minAngle
                while (angle <= mirror.maxAngle) {
                    mirror.angle = angle
                    if (testMirrorAngles(index + 1)) {
                        mirror.angle = origAngle
                        return true
                    }
                    angle += step
                }
                mirror.angle = origAngle
                return false
            }

            solvable = testMirrorAngles(0)

            if (!solvable) {
                unsolvableStages.add(stageNum)
            }
        }

        assertTrue(
            "The following stages could not be cleared by the solvability test: $unsolvableStages",
            unsolvableStages.isEmpty()
        )
    }
}
