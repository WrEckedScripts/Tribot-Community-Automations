package org.tribot.wrtiaracrafter.tasks

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepHotReaction
import nullablelib.core.query.TileObjects
import nullablelib.core.tabs.Inventory
import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.wrtiaracrafter.data.Altars
import org.tribot.script.sdk.Waiting as SdkWaiting
import org.tribot.script.sdk.util.Retry as SdkRetry

class EnterRuin(private val altar: Altars) : TaskContract {
    override val name: String
        get() = "Enter ruin"

    override fun perform(): Boolean {
        val playerLocation = ctx.client.localPlayer?.worldLocation ?: return false
        if (altar.entryLocation.distanceTo(playerLocation) > 10) {
            MoveToLocation(ctx, altar.entryLocation, arrivalRadius = 10).execute()
            return false
        }

        val talisman = Inventory.getItems().firstOrNull { it.id == altar.talismanId }
        if (talisman == null) {
            ctx.logger.info("No talisman found in inventory")
            return false
        }

        val altarObject = TileObjects.closestWithId(
            *altar.objectIds.toIntArray(),
            source = playerLocation,
            maxDistance = 15,
        ) ?: return false

        Inventory.clickItem(talisman.id, "Use")
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
