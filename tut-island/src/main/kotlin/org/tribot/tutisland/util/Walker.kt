package org.tribot.tutisland.util

import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.addon.dentistwalker.WalkingCondition
import org.tribot.script.sdk.AutomationSdk
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.types.WorldTile

object Walker {
    fun walkTo(tile: WorldTile): Boolean =
        AutomationSdk.getContext().addonLibraries.dentistWalker.walkTo(
            tile.toWorldPoint()
        ) { walkingState() }

    private fun walkingState(): WalkingCondition.State {
        enableRun()

        return WalkingCondition.State.CONTINUE
    }

    private fun enableRun() {
        if (Options.isRunEnabled()) return
        if (MyPlayer.getRunEnergy() < 10) return

        if (Options.setRunEnabled(true)) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                Options.isRunEnabled()
            }
        }
    }

    private fun WorldTile.toWorldPoint(): WorldPoint =
        WorldPoint(x, y, plane)
}