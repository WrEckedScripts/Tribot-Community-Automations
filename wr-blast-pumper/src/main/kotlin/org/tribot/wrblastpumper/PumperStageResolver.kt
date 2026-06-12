package org.tribot.wrblastpumper

import net.runelite.api.gameval.ObjectID
import nullablelib.core.query.TileObjects
import org.tribot.automation.script.ScriptContext
import org.tribot.wrblastpumper.data.BlastFurnaceObject

class PumperStageResolver(private val context: ScriptContext) {
    fun resolve(isRefreshDue: Boolean, isRefuelDue: Boolean): PumperStage =
        resolve(readState(isRefreshDue, isRefuelDue))

    private fun readState(isRefreshDue: Boolean, isRefuelDue: Boolean): PumperState {
        if (!context.login.isLoggedIn()) {
            return PumperState(isLoggedIn = false)
        }

        val player = context.client.localPlayer
            ?: return PumperState(isLoggedIn = false)
        val pump = BlastFurnaceObject.PUMP

        return PumperState(
            isLoggedIn = true,
            world = context.client.world,
            isPumpNearby = TileObjects.closestWithId(pump.objectId) != null,
            isPumping = player.animation == pump.playerAnimationId,
            isRefreshDue = isRefreshDue,
            isStoveRefillableNearby = TileObjects.closestWithId(
                ObjectID.BLAST_FURNACE_STOVE_LOW,
                ObjectID.BLAST_FURNACE_STOVE_MEDIUM,
                maxDistance = 8,
            ) != null,
            isRefuelDue = isRefuelDue,
        )
    }

    companion object {
        internal fun resolve(state: PumperState): PumperStage {
            if (!state.isLoggedIn) return PumperStage.LOGIN

            // To allow for custom worlds, below is disabled for the time being.
//            if (state.world !in PumpWorld.numbers) return PumperStage.UNSUPPORTED_WORLD

            if (state.isRefuelDue && state.isStoveRefillableNearby) return PumperStage.REFUEL_STOVE
            if (state.isRefreshDue && state.isPumpNearby) return PumperStage.REFRESH_PUMP
            if (state.isPumping) return PumperStage.PUMPING
            if (state.isPumpNearby) return PumperStage.OPERATE_PUMP
            return PumperStage.FIND_PUMP
        }
    }
}
