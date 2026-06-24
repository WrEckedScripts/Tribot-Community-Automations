package org.tribot.tutisland.tasks.bank

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class LeaveBank: Task {
    override val displayName = "Leaving Bank"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 525
    }

    override fun execute() {
        if (closePollBooth()) {
            return
        }

        val door = Query.gameObjects()
            .idEquals(9721)
            .actionEquals("Open")
            .isReachable
            .findClosest()
            .orElse(null)

        if (door == null) {
            Walker.walkTo(Constants.bankArea.randomTile)
            return
        }

        if (door.interact("Open")) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Query.npcs().nameEquals("Account Guide").isReachable.isAny
            }
        }
    }

    private fun closePollBooth(): Boolean {
        if (!Query.widgets().inRoots(928).isVisible().isAny) return false

        Widgets.get(928, 4).ifPresent { close ->
            if (!close.click("Close")) {
                close.click()
            }
        }

        Waiting.waitUntil(TutPreferences.longDelayMs()) { !Query.widgets().inRoots(928).isVisible().isAny }
        return true
    }
}