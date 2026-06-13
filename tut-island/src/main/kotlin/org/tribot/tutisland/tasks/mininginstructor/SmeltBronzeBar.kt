package org.tribot.tutisland.tasks.mininginstructor

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.*
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class SmeltBronzeBar: Task {
    override val displayName = "Smelting Bronze Bar"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 320 &&
                Inventory.getCount(ItemID.TIN_ORE) > 0 &&
                Inventory.getCount(ItemID.COPPER_ORE) > 0 &&
                Inventory.getCount(ItemID.BRONZE_BAR) == 0
    }

    override fun execute() {
        if (MyPlayer.isAnimating()) return

        if (MakeScreen.isOpen()) {
            MakeScreen.makeAll(ItemID.BRONZE_BAR)
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Inventory.getCount(ItemID.BRONZE_BAR) > 0
            }
            return
        }

        val itemId = TutPreferences.choose("smelt_bronze_ore", ItemID.TIN_ORE, ItemID.COPPER_ORE)
        val item = Query.inventory().idEquals(itemId).findFirst().orElse(null) ?: return
        val furnace = Query.gameObjects().idEquals(10082).isReachable.findClosest().orElse(null) ?: return

        if (item.click("Use") && Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameState.isAnyItemSelected() }) {
            furnace.interact("Use")
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Inventory.getCount(ItemID.BRONZE_BAR) > 0
            }
        }
    }
}