package org.tribot.tutisland.tasks.survivalexpert

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class CookShrimp: Task {
    override val displayName = "Cooking Shrimp"
    override val priority = 0

    override fun canRun(): Boolean {
        return Inventory.getCount(ItemID.NEWBIERAW_SHRIMP) > 0 &&
                Query.gameObjects().nameEquals("Fire").isReachable.isAny &&
                GameState.getSetting(281) == 90
    }

    override fun execute() {
        if (MyPlayer.isAnimating()) return

        val shrimp = Query.inventory().idEquals(ItemID.NEWBIERAW_SHRIMP).findFirst().orElse(null) ?: return
        val fire = Query.gameObjects().idEquals(26185).isReachable.findClosest().orElse(null) ?: return

        if (shrimp.click("Use") && Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameState.isAnyItemSelected() }) {
            fire.interact("Use")
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Inventory.getCount(ItemID.NEWBIERAW_SHRIMP) == 0
            }
        }
    }
}