package org.tribot.gemstoneminer.tasks

import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.util.GemStoneMinerPreferences
import org.tribot.gemstoneminer.util.Walker
import org.tribot.gemstoneminer.util.data.Pickaxe
import org.tribot.gemstoneminer.util.data.Vars
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.script.sdk.*
import org.tribot.script.sdk.query.Query

class MineGemRocks: Task {
    override val displayName = "Mining Gem Rocks"
    override val priority = 0

    private val gemRockObjectIds = intArrayOf(11390, 11391, 11381, 11380)

    override fun canRun(): Boolean {
        return Vars.BankingDone
    }

    override fun execute() {
        val mineLevel = Settings.mineLevel

        if (Inventory.isFull() || !hasPickaxe()) {
            Vars.BankingDone = false
            return
        }

        if (Region.getCurrentRegionID() != mineLevel.regionId && !hasGemRockObject()) {
            Log.info("[GemStoneMiner] Walking to ${mineLevel.displayName} Gem rocks.")
            Walker.walkTo(mineLevel.walkingTile, 2)
            return
        }

        if (MyPlayer.isAnimating()) {
            return
        }

        val rock = Query.gameObjects()
            .nameEquals("Gem rocks")
            .isReachable
            .findClosestByPathDistance()
            .orElse(null)

        if (rock == null) {
            if (!hasGemRockObject()) {
                Walker.walkTo(mineLevel.walkingTile, 2)
            }
            return
        }

        val xpBefore = Skill.MINING.xp
        if (rock.interact("Mine")) {
            Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs()) {
                Skill.MINING.xp > xpBefore || Inventory.isFull()
            }
        }
    }

    fun hasPickaxe(): Boolean =
        Inventory.contains(*Pickaxe.ids.toIntArray()) ||
                Equipment.contains(*Pickaxe.ids.toIntArray())

    private fun hasGemRockObject(): Boolean =
        Query.gameObjects()
            .idEquals(*gemRockObjectIds)
            .isAny
}