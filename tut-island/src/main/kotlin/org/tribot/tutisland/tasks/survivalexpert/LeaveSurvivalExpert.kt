package org.tribot.tutisland.tasks.survivalexpert

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class LeaveSurvivalExpert: Task {
    override val displayName = "Leaving Survival Expert"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 120
    }

    override fun execute() {
        val gate = Query.gameObjects()
            .idEquals(9470)
            .actionEquals("Open")
            .isReachable
            .findClosest()
            .orElse(null)

        if (gate == null) {
            Walker.walkTo(Constants.survivalGuideArea.randomTile)
            return
        }

        if (gate.interact("Open")) {
            Waiting.waitUntil(TutPreferences.longDelayMs() * 3) {
                !Query.npcs().nameEquals("Survival Expert").isReachable.isAny
            }
        }
    }
}