package org.tribot.wrtiaracrafter.tasks

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepColdReaction
import nullablelib.core.input.click
import nullablelib.core.query.TileObjects
import nullablelib.core.tabs.Inventory
import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.wrtiaracrafter.data.Altars

class CraftTiara(private val altar: Altars) : TaskContract {
    override val name: String
        get() = "Craft tiara"

    override fun execute(): Boolean {
        if (
            Inventory.getCount(altar.talismanId) == 0 ||
            Inventory.getCount(altar.tiaraId) == 0
        ) {
            ctx.logger.info("Missing a talisman or tiara before crafting")
            return false
        }

        val altarObject = altar.objectIds.firstNotNullOfOrNull { objectId ->
            TileObjects.closestWithId(objectId)
        } ?: return false

        Inventory.clickItem(altar.talismanId, "Use")
        altarObject.click("Use")
        sleepColdReaction()

        return true
    }
}
