package org.tribot.tutisland.tasks.combatinstructor

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class EquipSwordAndShield: Task {
    override val displayName = "Equipping Sword & Shield"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 420 &&
                (Inventory.getCount(ItemID.BRONZE_SWORD) > 0 || Inventory.getCount(ItemID.WOODEN_SHIELD) > 0)
    }

    override fun execute() {
        val swordFirst = TutPreferences.orderAB("equip_sword_shield_order")

        fun equipSword(): Boolean {
            if (Inventory.getCount(ItemID.BRONZE_SWORD) <= 0) return false
            return Equipment.equip(ItemID.BRONZE_SWORD) &&
                    Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                        Equipment.getCount(ItemID.BRONZE_SWORD) > 0
                    }
        }

        fun equipShield(): Boolean {
            if (Inventory.getCount(ItemID.WOODEN_SHIELD) <= 0) return false
            return Equipment.equip(ItemID.WOODEN_SHIELD) &&
                    Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                        Equipment.getCount(ItemID.WOODEN_SHIELD) > 0
                    }
        }

        if (swordFirst) {
            if (equipSword()) return
            equipShield()
        } else {
            if (equipShield()) return
            equipSword()
        }
    }
}