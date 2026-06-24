package org.tribot.tutisland.tasks.combatinstructor

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class EquipDagger: Task {
    override val displayName = "Equipping Dagger"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 405 &&
                Inventory.getCount(ItemID.BRONZE_DAGGER) > 0
    }

    override fun execute() {
        if (Equipment.equip(ItemID.BRONZE_DAGGER)) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                Equipment.getCount(ItemID.BRONZE_DAGGER) > 0
            }
        }
    }
}