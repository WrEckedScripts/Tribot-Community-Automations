package org.tribot.wrtiaracrafter.tasks

import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.addon.dentistwalker.WalkingCondition
import org.tribot.community.commons.playerstate.PlayerEnergyHelper
import org.tribot.wrscript.utilities.tasks.TaskContract

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

        ctx.logger.info("Walking to $location")

        val walkSucceeded = ctx.addonLibraries.dentistWalker.walkTo(location, {
            PlayerEnergyHelper.enable()

            if (isAtLocation()) {
                return@walkTo WalkingCondition.State.SUCCESS
            }

            return@walkTo WalkingCondition.State.CONTINUE
        })

        val arrived = isAtLocation()
        if (!walkSucceeded || !arrived) {
            ctx.logger.warn(
                "Failed to reach $location within $arrivalRadius tiles " +
                        "(walkerSucceeded=$walkSucceeded, arrived=$arrived)"
            )
            return false
        }

        ctx.logger.info("Reached $location")
        return true
    }
}
