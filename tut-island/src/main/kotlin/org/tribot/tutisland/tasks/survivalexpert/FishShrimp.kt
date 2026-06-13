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

class FishShrimp: Task {
    override val displayName = "Fishing Shrimp"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 40 &&
                Inventory.getCount(ItemID.NEWBIERAW_SHRIMP) == 0
    }

    override fun execute() {
        if (!Query.npcs().idEquals(3317).isReachable.isAny) {
            Walker.walkTo(Constants.survivalGuideArea.randomTile)
            return
        }

        if (!MyPlayer.isAnimating()) {
            Query.npcs()
                .idEquals(3317)
                .actionEquals("Net")
                .isReachable
                .findBestInteractable()
                .ifPresent { fishingSpot ->
                    if (fishingSpot.interact("Net")) {
                        Waiting.waitUntil(TutPreferences.longDelayMs()) {
                            Inventory.getCount(ItemID.NEWBIERAW_SHRIMP) > 0
                        }
                    }
                }
        }
    }
}