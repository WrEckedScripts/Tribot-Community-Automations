package org.tribot.tutisland.tasks.accountguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class LeaveAccountGuide: Task {
    override val displayName = "Leaving Account Guide"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 540
    }

    override fun execute() {
        val door = Query.gameObjects()
            .idEquals(9722)
            .actionEquals("Open")
            .isReachable
            .findClosest()
            .orElse(null)

        if (door == null) {
            Walker.walkTo(Constants.accountGuideArea.randomTile)
            return
        }

        if (door.interact("Open")) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                !Query.npcs().nameEquals("Account Guide").isReachable.isAny
            }
        }
    }
}