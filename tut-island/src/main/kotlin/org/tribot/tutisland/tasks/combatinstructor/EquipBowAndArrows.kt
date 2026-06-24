package org.tribot.tutisland.tasks.combatinstructor

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class EquipBowAndArrows: Task {
    override val displayName = "Equipping Bow & Arrows"
    override val priority = 1

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 480 &&
                (Inventory.getCount(ItemID.SHORTBOW) > 0 || Inventory.getCount(ItemID.BRONZE_ARROW) > 0)
    }

    override fun execute() {
        val bowFirst = TutPreferences.orderAB("equip_bow_arrows_order")

        fun equipBow(): Boolean {
            if (Inventory.getCount(ItemID.SHORTBOW) <= 0) return false
            return Equipment.equip(ItemID.SHORTBOW) &&
                    Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                        Equipment.getCount(ItemID.SHORTBOW) > 0
                    }
        }

        fun equipArrows(): Boolean {
            if (Inventory.getCount(ItemID.BRONZE_ARROW) <= 0) return false
            return Equipment.equip(ItemID.BRONZE_ARROW) &&
                    Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                        Equipment.getCount(ItemID.BRONZE_ARROW) > 0
                    }
        }

        if (bowFirst) {
            if (equipBow()) return
            equipArrows()
        } else {
            if (equipArrows()) return
            equipBow()
        }
    }
}