package org.tribot.wrblastpumper.tasks

import nullablelib.NullableLib.ctx
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

        // Operate action seems to ditch sometimes, no clue why yet.
        Definitions.getObject(pump.id)?.actions?.forEach {
            ctx.logger.info("Action: $it")
        }
        if (false == Definitions.getObject(pump.id)?.actions?.contains(pumpData.action)) return false

        if (!ctx.interaction.interact(pump, pumpData.action)) {
            return false
        }

        Mouse.leaveScreen()

        return Waiting.waitUntil(3_750) {
            ctx.client.localPlayer?.animation == pumpData.playerAnimationId
        }
    }
}
