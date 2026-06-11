package org.tribot.wrtiaracrafter.tasks

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepHotReaction
import nullablelib.core.query.TileObjects
import nullablelib.core.tabs.Inventory
import org.tribot.script.sdk.Waiting as SdkWaiting
import org.tribot.script.sdk.util.Retry as SdkRetry
import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.wrtiaracrafter.data.Altars

class EnterRuin(private val altar: Altars) : TaskContract {
    override val name: String
        get() = "Enter ruin"

    override fun execute(): Boolean {
        val playerLocation = ctx.client.localPlayer?.worldLocation ?: return false
        if (altar.entryLocation.distanceTo(playerLocation) > 10) {
            MoveToLocation(ctx, altar.entryLocation, arrivalRadius = 10).execute()
            return false
        }

        // Query inventory for random talisman
        val talisman = Inventory.getItems().firstOrNull { it.id == altar.talismanId }
        if (talisman == null) {
            ctx.logger.info("No talisman found in inventory")
            return false
        }

        Inventory.clickItem(talisman.id, "Use")

        val altarObject = altar.objectIds.firstNotNullOfOrNull { objectId ->
            TileObjects.closestWithId(objectId)
        } ?: return false

        val clicked = SdkRetry.retry(3) {
            if (ctx.interaction.click(altarObject, "Use")) {
                true
            } else {
                sleepHotReaction()
                false
            }
        }
        if (!clicked) return false

        return SdkWaiting.waitUntil(7_500) {
            val currentLocation = ctx.client.localPlayer?.worldLocation
                ?: return@waitUntil false
            altar.exitLocation.distanceTo(currentLocation) <= 15
        }
    }
}
