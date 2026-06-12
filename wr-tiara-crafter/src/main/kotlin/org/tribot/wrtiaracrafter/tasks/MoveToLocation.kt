package org.tribot.wrtiaracrafter.tasks

import net.runelite.api.coords.WorldPoint
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.VarPlayerID
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.addon.dentistwalker.WalkingCondition
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrtiaracrafter.contracts.TaskContract

class MoveToLocation(
    private val ctx: ScriptContext,
    private val location: WorldPoint,
    private val arrivalRadius: Int = 15,
) : TaskContract {
    override val name: String
        get() = "Move to $location"

    private fun isAtLocation(): Boolean {
        val player = ctx.client.localPlayer ?: return false
        val playerLocation = player.worldLocation

        return location.distanceTo(playerLocation) <= arrivalRadius
    }

    override fun perform(): Boolean {
        if (isAtLocation()) {
            ctx.logger.info("Already at $location")
            return true
        }

        val targetTile = fuzzTile(location)

        ctx.logger.info("Walking to $targetTile")

        ctx.addonLibraries.dentistWalker.walkTo(targetTile, {
            // Randomly enable running
            if ((ctx.client.energy / 100) >= TribotRandom.uniform(44, 63) && ctx.client.getVarpValue(VarPlayerID.OPTION_RUN) != 1) {
                ctx.logger.debug("Toggling run at ${ctx.client.energy}")
                val toggle = ctx.client.getWidget(InterfaceID.Orbs.RUNBUTTON)
                    ?: return@walkTo WalkingCondition.State.CONTINUE

                if (!ctx.interaction.click(toggle)) {
                    return@walkTo WalkingCondition.State.CONTINUE
                }
            }

            if (isAtLocation()) {
                return@walkTo WalkingCondition.State.SUCCESS
            }

            return@walkTo WalkingCondition.State.CONTINUE
        })

        ctx.logger.info("Walked to $targetTile")

        return isAtLocation()
    }

    private fun fuzzTile(tile: WorldPoint, radius: Int = 4): WorldPoint {
        val x = (tile.x - radius..tile.x + radius).random()
        val y = (tile.y - radius..tile.y + radius).random()
        return WorldPoint(x, y, tile.plane)
    }
}
