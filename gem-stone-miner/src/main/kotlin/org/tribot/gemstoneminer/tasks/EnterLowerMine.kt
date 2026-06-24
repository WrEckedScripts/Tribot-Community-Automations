package org.tribot.gemstoneminer.tasks

import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.util.GemStoneMinerPreferences
import org.tribot.gemstoneminer.util.Walker
import org.tribot.gemstoneminer.util.data.MineLevel
import org.tribot.gemstoneminer.util.data.Pickaxe
import org.tribot.gemstoneminer.util.data.Vars
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Region
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query

class EnterLowerMine: Task {
    override val displayName = "Entering Lower Mine"
    override val priority = 10

    override fun canRun(): Boolean {
        return Vars.BankingDone &&
            Settings.mineLevel == MineLevel.LOWER &&
            Region.getCurrentRegionID() != MineLevel.LOWER.regionId
    }

    override fun execute() {
        if (Inventory.isFull() || !hasPickaxe()) {
            Vars.BankingDone = false
            return
        }

        val ladder = Query.gameObjects()
            .idEquals(23586)
            .findClosestByPathDistance()
            .orElse(null)

        if (ladder == null) {
            Walker.walkTo(MineLevel.UPPER.walkingTile, 2)
            return
        }

        if (ladder.interact("Climb-down")) {
            Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 2) {
                Region.getCurrentRegionID() == MineLevel.LOWER.regionId
            }
        }
    }

    fun hasPickaxe(): Boolean =
        Inventory.contains(*Pickaxe.ids.toIntArray()) ||
                Equipment.contains(*Pickaxe.ids.toIntArray())
}