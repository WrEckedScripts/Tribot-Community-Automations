package org.tribot.tutisland.tasks.masterchef

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class BakeBread: Task {
    override val displayName = "Baking Bread"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 160 &&
                Inventory.getCount(ItemID.BREAD_DOUGH) > 0
    }

    override fun execute() {
        if (MyPlayer.isAnimating()) return

        val dough = Query.inventory().idEquals(ItemID.BREAD_DOUGH).findFirst().orElse(null) ?: return
        val range = Query.gameObjects().idEquals(9736).isReachable.findClosest().orElse(null) ?: return

        if (dough.click("Use") && Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameState.isAnyItemSelected() }) {
            range.interact("Use")
            Waiting.waitUntil(TutPreferences.longDelayMs() * 2) {
                Inventory.getCount(ItemID.BREAD) > 0
            }
        }
    }
}