package org.tribot.gemstoneminer.util

import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.addon.dentistwalker.WalkingCondition
import org.tribot.script.sdk.AutomationSdk
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.util.TribotRandom

object Walker {
    fun walkTo(tile: WorldTile): Boolean =
        AutomationSdk.getContext().addonLibraries.dentistWalker.walkTo(
            tile.toWorldPoint()
        ) { walkingState() }

    fun walkTo(tile: WorldTile, randomRadius: Int): Boolean =
        walkTo(randomized(tile, randomRadius))

    fun walkToBank(): Boolean =
        AutomationSdk.getContext().addonLibraries.dentistWalker.walkToBank { walkingState() }

    private fun walkingState(): WalkingCondition.State {
        enableRun()
        return WalkingCondition.State.CONTINUE
    }

    private fun enableRun() {
        if (Options.isRunEnabled()) return
        if (MyPlayer.getRunEnergy() < 10) return

        if (Options.setRunEnabled(true)) {
            Waiting.waitUntil(GemStoneMinerPreferences.mediumDelayMs()) {
                Options.isRunEnabled()
            }
        }
    }

    private fun WorldTile.toWorldPoint(): WorldPoint =
        WorldPoint(x, y, plane)

    private fun randomized(tile: WorldTile, radius: Int): WorldTile =
        WorldTile(
            tile.x + TribotRandom.uniform(-radius, radius),
            tile.y + TribotRandom.uniform(-radius, radius),
            tile.plane
        )
}