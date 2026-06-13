package org.tribot.tutisland.tasks.questguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class LeaveQuestGuide: Task {
    override val displayName = "Leaving Quest Guide"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 250
    }

    override fun execute() {
        val ladder = Query.gameObjects()
            .idEquals(9726)
            .actionEquals("Climb-down")
            .isReachable
            .findClosest()
            .orElse(null)

        if (ladder == null) {
            Walker.walkTo(Constants.questGuideArea.randomTile)
            return
        }

        if (ladder.interact("Climb-down")) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                !Query.npcs().nameEquals("Quest Guide").isReachable.isAny
            }
        }
    }
}