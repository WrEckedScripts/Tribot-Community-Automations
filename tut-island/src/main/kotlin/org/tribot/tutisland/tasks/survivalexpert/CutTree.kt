package org.tribot.tutisland.tasks.survivalexpert

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class CutTree: Task {
    override val displayName = "Cutting Tree"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 70 &&
                Inventory.getCount(ItemID.NEWBIELOGS) == 0
    }

    override fun execute() {
        if (!MyPlayer.isAnimating()) {
            val tree = Query.gameObjects()
                .idEquals(9730)
                .actionEquals("Chop down")
                .isReachable
                .findClosest()
                .orElse(null)

            if (tree == null) {
                Walker.walkTo(Constants.survivalGuideArea.randomTile)
                return
            }

            if (tree.interact("Chop down")) {
                Waiting.waitUntil(TutPreferences.longDelayMs()) {
                    Inventory.getCount(ItemID.NEWBIELOGS) > 0
                }
            }
        }
    }
}