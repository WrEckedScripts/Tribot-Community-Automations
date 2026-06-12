package org.tribot.wrblastpumper

import nullablelib.core.query.TileObjects
import org.tribot.automation.script.ScriptContext
import org.tribot.wrblastpumper.data.BlastFurnaceObject
import org.tribot.wrblastpumper.data.PumpWorld

class PumperStageResolver(private val context: ScriptContext) {
    fun resolve(isRefreshDue: Boolean): PumperStage = resolve(readState(isRefreshDue))

    private fun readState(isRefreshDue: Boolean): PumperState {
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
        )
    }

    companion object {
        internal fun resolve(state: PumperState): PumperStage {
            if (!state.isLoggedIn) return PumperStage.LOGIN
            if (state.world !in PumpWorld.numbers) return PumperStage.UNSUPPORTED_WORLD
            if (state.isRefreshDue && state.isPumpNearby) return PumperStage.REFRESH_PUMP
            if (state.isPumping) return PumperStage.PUMPING
            if (state.isPumpNearby) return PumperStage.OPERATE_PUMP
            return PumperStage.FIND_PUMP
        }
    }
}
