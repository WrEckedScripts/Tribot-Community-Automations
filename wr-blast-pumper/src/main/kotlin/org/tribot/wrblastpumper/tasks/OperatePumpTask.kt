package org.tribot.wrblastpumper.tasks

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepClickReaction
import nullablelib.antiban.sleepIdleWakeup
import nullablelib.core.definition.Definitions
import nullablelib.core.query.TileObjects
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.input.Mouse
import org.tribot.wrblastpumper.data.BlastFurnaceObject
import org.tribot.wrscript.utilities.tasks.TaskContract

class OperatePumpTask : TaskContract {
    override val name = "Operating pump"

    override fun perform(): Boolean {
        val pumpData = BlastFurnaceObject.PUMP
        val pump = TileObjects.closestWithId(pumpData.objectId)
            ?: return false

        if (false == Definitions.getObject(pump.id)?.actions?.contains(pumpData.action)) {
            ctx.logger.debug("Pump does not have action ${pumpData.action}")
            return false
        }

        if (!ctx.interaction.interact(pump, pumpData.action)) {
            ctx.logger.debug("Failed to interact with pump, waiting for a brief moment before retrying again")
            sleepIdleWakeup()
            return false
        }

        val startedPumping = Waiting.waitUntil(3_750) {
            ctx.client.localPlayer?.animation == pumpData.playerAnimationId
        }

        if (startedPumping) {
            ctx.logger.info("Pumping started, leaving screen.")
            sleepClickReaction()
            Mouse.leaveScreen()
        }

        return startedPumping
    }
}
