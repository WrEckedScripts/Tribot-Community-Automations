package org.tribot.tutisland.tasks.masterchef

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class LeaveCook: Task {
    override val displayName = "Leaving Master Chef"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 170
    }

    override fun execute() {
        val door = Query.gameObjects()
            .idEquals(9710)
            .actionEquals("Open")
            .isReachable
            .findClosest()
            .orElse(null)

        if (door == null) {
            Walker.walkTo(Constants.masterChefArea.randomTile)
            return
        }

        if (door.interact("Open")) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                !Query.npcs().nameEquals("Master Chef").isReachable.isAny
            }
        }
    }
}