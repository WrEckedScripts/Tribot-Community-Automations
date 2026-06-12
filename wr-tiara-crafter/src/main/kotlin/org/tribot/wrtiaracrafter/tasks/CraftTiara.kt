package org.tribot.wrtiaracrafter.tasks

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepColdReaction
import nullablelib.core.input.click
import nullablelib.core.query.TileObjects
import nullablelib.core.tabs.Inventory
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrtiaracrafter.antiban.BreaksHelper
import org.tribot.wrtiaracrafter.contracts.TaskContract
import org.tribot.wrtiaracrafter.data.Altars
import org.tribot.script.sdk.Waiting as SdkWaiting

class CraftTiara(private val altar: Altars) : TaskContract {
    override val name: String
        get() = "Craft tiara"

    override fun perform(): Boolean {
        if (!waitUntilPlayerStopsMoving()) {
            ctx.logger.info("Player did not stop moving before crafting; retrying")
            return false
        }

        if (
            Inventory.getCount(altar.talismanId) == 0 ||
            Inventory.getCount(altar.tiaraId) == 0
        ) {
            ctx.logger.info("Missing a talisman or tiara before crafting")
            return false
        }

        BreaksHelper.afkBreak(
            probabilityRange = 0.02..0.06
        )

        val playerLocation = ctx.client.localPlayer?.worldLocation ?: return false
        val altarObject = TileObjects.closestWithId(
            *altar.objectIds.toIntArray(),
            source = playerLocation,
            maxDistance = 15,
        ) ?: return false

        Inventory.clickItem(altar.talismanId, "Use")
        altarObject.click("Use")

        sleepColdReaction()

        return true
    }

    private fun waitUntilPlayerStopsMoving(): Boolean {
        val player = ctx.client.localPlayer ?: return false
        if (player.poseAnimation == player.idlePoseAnimation) {
            return true
        }

        return SdkWaiting.waitUntil(3_500, TribotRandom.uniform(450, 1_250)) {
            val currentPlayer = ctx.client.localPlayer
                ?: return@waitUntil false
            currentPlayer.poseAnimation == currentPlayer.idlePoseAnimation
        }
    }
}
