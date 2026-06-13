package org.tribot.tutisland.tasks.masterchef

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class MakeDough: Task {
    override val displayName = "Making Dough"
    override val priority = 0

    override fun canRun(): Boolean {
        return Inventory.getCount(ItemID.NEWBIE_POT_FLOUR) > 0 &&
                Inventory.getCount(ItemID.BUCKET_WATER) > 0 &&
                GameState.getSetting(281) == 150
    }

    override fun execute() {
        val waterFirst = TutPreferences.orderAB("make_dough_order")

        val (src, dst) = if (waterFirst) {
            ItemID.BUCKET_WATER to ItemID.NEWBIE_POT_FLOUR
        } else {
            ItemID.NEWBIE_POT_FLOUR to ItemID.BUCKET_WATER
        }

        val source = Query.inventory().idEquals(src).findFirst().orElse(null) ?: return
        val target = Query.inventory().idEquals(dst).findFirst().orElse(null) ?: return

        if (source.click("Use") && Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameState.isAnyItemSelected() }) {
            target.click()
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                Inventory.getCount(ItemID.NEWBIE_POT_FLOUR) == 0
            }
        }
    }
}