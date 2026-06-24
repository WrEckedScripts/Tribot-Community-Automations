package org.tribot.tutisland.tasks.combatinstructor

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class LeaveCombatInstructor: Task {
    override val displayName = "Leaving Combat Instructor"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 500
    }

    override fun execute() {
        val ladder = Query.gameObjects()
            .idEquals(9727)
            .actionEquals("Climb-up")
            .isReachable
            .maxDistance(8.0)
            .findClosest()
            .orElse(null)

        if (ladder == null) {
            Walker.walkTo(Constants.combatInstructorLadderArea.randomTile)
            return
        }

        if (ladder.interact("Climb-up")) {
            Waiting.waitUntil(TutPreferences.longDelayMs() * 5) {
                !Query.gameObjects().idEquals(9720).isReachable.isAny
            }
        }
    }
}