package org.tribot.playermover.tasks

import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.ScriptContext
import org.tribot.playermover.contracts.TaskContract

class MoveToLocation(
    private val ctx: ScriptContext,
    private val location: WorldPoint,
) : TaskContract {
    override val name: String
        get() = "Move to $location"

    fun satisfied(): Boolean {
        val player = ctx.client.localPlayer ?: return false
        val playerLocation = player.worldLocation

        return location.distanceTo(playerLocation) <= 15
    }

    override fun execute(): Boolean {
        if (satisfied()) {
            ctx.logger.info("Already at $location")
            return true
        }

        val targetTile = fuzzTile(location)

        ctx.logger.info("Walking to $targetTile")

        ctx.addonLibraries.dentistWalker.walkTo(targetTile)
        ctx.logger.info("Walked to $targetTile")

        return satisfied()
    }

    private fun fuzzTile(tile: WorldPoint, radius: Int = 4): WorldPoint {
        val x = (tile.x - radius..tile.x + radius).random()
        val y = (tile.y - radius..tile.y + radius).random()
        return WorldPoint(x, y, tile.plane)
    }
}
