package org.tribot.tutisland.tasks.mininginstructor

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Widget
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class MakeBronzeDagger: Task {
    override val displayName = "Making Bronze Dagger"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) in 340..350 &&
                Inventory.getCount(ItemID.BRONZE_BAR) > 0 &&
                Inventory.getCount(ItemID.HAMMER) > 0 &&
                Inventory.getCount(ItemID.BRONZE_DAGGER) == 0
    }

    override fun execute() {
        if (MyPlayer.isAnimating()) return

        val daggerWidget = bronzeDaggerWidget()

        if (daggerWidget == null) {
            val anvil = Query.gameObjects()
                .idEquals(2097)
                .actionEquals("Smith")
                .isReachable
                .findClosest()
                .orElse(null)

            if (anvil == null) {
                Walker.walkTo(Constants.miningInstructorArea.randomTile)
                return
            }

            if (anvil.interact("Smith")) {
                Waiting.waitUntil(TutPreferences.longDelayMs() * 2) {
                    bronzeDaggerWidget() != null
                }
            }
            return
        }

        if (daggerWidget.click("Smith")) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                Inventory.getCount(ItemID.BRONZE_DAGGER) > 0
            }
        }
    }

    private fun bronzeDaggerWidget(): Widget? =
        Query.widgets()
            .inIndexPath(312, 9)
            .actionEquals("Smith")
            .isVisible()
            .findFirst()
            .orElse(null)
}