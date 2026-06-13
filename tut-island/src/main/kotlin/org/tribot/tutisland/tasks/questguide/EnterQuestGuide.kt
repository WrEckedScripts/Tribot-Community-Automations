package org.tribot.tutisland.tasks.questguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class EnterQuestGuide: Task {
    override val displayName = "Entering Quest Guide Area"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) in 183..200
    }

    override fun execute() {
        val door = Query.gameObjects()
            .idEquals(9716)
            .actionEquals("Open")
            .isReachable
            .findClosest()
            .orElse(null)

        if (door == null) {
            Walker.walkTo(Constants.questGuideArea.randomTile)
            return
        }

        if (door.interact("Open")) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Query.npcs().nameEquals("Quest Guide").isReachable.isAny
            }
        }
    }
}