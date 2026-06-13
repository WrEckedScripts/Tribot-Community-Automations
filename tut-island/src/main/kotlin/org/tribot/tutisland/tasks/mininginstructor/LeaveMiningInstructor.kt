package org.tribot.tutisland.tasks.mininginstructor

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class LeaveMiningInstructor: Task {
    override val displayName = "Leaving Mining Instructor"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 360
    }

    override fun execute() {
        val gate = Query.gameObjects()
            .idEquals(9718)
            .actionEquals("Open")
            .isReachable
            .findClosest()
            .orElse(null)

        if (gate == null) {
            Walker.walkTo(Constants.miningInstructorArea.randomTile)
            return
        }

        if (gate.interact("Open")) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Query.npcs().nameEquals("Combat Instructor").isReachable.isAny
            }
        }
    }
}