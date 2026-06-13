package org.tribot.tutisland.tasks.mininginstructor

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

class MineTin: Task {
    override val displayName = "Mining Tin"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 300 &&
                Inventory.getCount(ItemID.TIN_ORE) == 0
    }

    override fun execute() {
        if (!MyPlayer.isAnimating()) {
            val rocks = Query.gameObjects()
                .nameEquals("Tin rocks")
                .actionEquals("Mine")
                .isReachable
                .findClosest()
                .orElse(null)

            if (rocks == null) {
                Walker.walkTo(Constants.miningInstructorArea.randomTile)
                return
            }

            if (rocks.interact("Mine")) {
                Waiting.waitUntil(TutPreferences.longDelayMs()) {
                    Inventory.getCount(ItemID.TIN_ORE) > 0
                }
            }
        }
    }
}