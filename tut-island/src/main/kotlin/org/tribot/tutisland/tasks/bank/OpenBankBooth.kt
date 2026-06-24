package org.tribot.tutisland.tasks.bank

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class OpenBankBooth: Task {
    override val displayName = "Opening Bank Booth"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 510
    }

    override fun execute() {
        if (Bank.isOpen()) return

        val booth = Query.gameObjects()
            .idEquals(10083)
            .actionEquals("Use")
            .isReachable
            .findClosest()
            .orElse(null)

        if (booth == null) {
            Walker.walkTo(Constants.bankArea.randomTile)
            return
        }

        if (booth.interact("Use")) {
            Waiting.waitUntil(TutPreferences.longDelayMs() * 5) {
                GameState.getSetting(281) != 510
            }
        }
    }
}