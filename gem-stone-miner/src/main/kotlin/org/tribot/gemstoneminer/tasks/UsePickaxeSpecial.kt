package org.tribot.gemstoneminer.tasks

import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.util.GemStoneMinerPreferences
import org.tribot.gemstoneminer.util.data.Pickaxe
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.Region
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query

class UsePickaxeSpecial: Task {
    override val displayName = "Using Pickaxe Special"
    override val priority = 1

    override fun canRun(): Boolean {
        return Equipment.contains(*Pickaxe.ids.toIntArray()) &&
                !Combat.isSpecialAttackEnabled() &&
                Combat.canUseSpecialAttack() &&
                isNearGemRocks()
    }

    override fun execute() {
        if (Combat.activateSpecialAttack()) {
            Waiting.waitUntil(GemStoneMinerPreferences.mediumDelayMs()) { !Combat.canUseSpecialAttack() }
        }
    }

    private fun isNearGemRocks(): Boolean {
        val mineLevel = Settings.mineLevel
        if (Region.getCurrentRegionID() != mineLevel.regionId) {
            return false
        }

        return Query.gameObjects()
            .nameEquals("Gem rocks")
            .isReachable
            .maxDistance(2.0)
            .isAny
    }
}