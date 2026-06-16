package org.tribot.gemstoneminer.tasks

import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.util.GemStoneMinerPreferences
import org.tribot.gemstoneminer.util.Walker
import org.tribot.gemstoneminer.util.data.Constants
import org.tribot.gemstoneminer.util.data.MineLevel
import org.tribot.gemstoneminer.util.data.Vars
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Region
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.query.Query

class LeaveLowerMine: Task {
    override val displayName = "Leaving Lower Mine"
    override val priority = 105

    override fun canRun(): Boolean {
        return Region.getCurrentRegionID() == MineLevel.LOWER.regionId &&
                (!Vars.BankingDone || Settings.mineLevel == MineLevel.UPPER)
    }

    override fun execute() {
        // Close deposit box if opened before interacting with ladder
        if (Bank.isDepositBoxOpen()) {
            Widgets.get(192, 1, 11).ifPresent { widget -> widget.click() }
            Waiting.waitUntil(GemStoneMinerPreferences.mediumDelayMs()) { !Bank.isDepositBoxOpen() }
            return
        }

        val ladder = Query.gameObjects()
            .idEquals(23584)
            .findClosestByPathDistance()
            .orElse(null)

        if (ladder == null) {
            Walker.walkTo(Constants.lowerMineExitLadderTile, 1)
            return
        }

        if (ladder.interact("Climb-up")) {
            Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 2) {
                Region.getCurrentRegionID() != MineLevel.LOWER.regionId
            }
        }
    }
}