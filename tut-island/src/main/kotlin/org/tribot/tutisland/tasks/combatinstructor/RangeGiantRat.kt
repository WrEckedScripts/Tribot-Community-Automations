package org.tribot.tutisland.tasks.combatinstructor

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Npc
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class RangeGiantRat: Task {
    override val displayName = "Ranging Giant Rat"
    override val priority = 0

    private var target: Npc? = null
    private var attackStarted = false

    override fun canRun(): Boolean {
        val step = GameState.getSetting(281)
        if (step !in 480..490) return false

        return hasOnPlayerOrInventory(ItemID.SHORTBOW) &&
                hasOnPlayerOrInventory(ItemID.BRONZE_ARROW)
    }

    override fun execute() {
        if (Inventory.getCount(ItemID.SHORTBOW) > 0) {
            equip(ItemID.SHORTBOW)
            return
        }

        if (Inventory.getCount(ItemID.BRONZE_ARROW) > 0) {
            equip(ItemID.BRONZE_ARROW)
            return
        }

        if (!Constants.combatInstructorArea.contains(MyPlayer.getTile())) {
            Walker.walkTo(Constants.combatInstructorArea.center)
            return
        }

        if (waitForDeath()) {
            return
        }

        if (needsNewTarget()) {
            acquireTarget()
            target ?: return
        }

        ensureCombat()
    }

    private fun hasOnPlayerOrInventory(itemId: Int): Boolean =
        Equipment.getCount(itemId) > 0 || Inventory.getCount(itemId) > 0

    private fun equip(itemId: Int): Boolean =
        Equipment.equip(itemId) &&
                Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                    Equipment.getCount(itemId) > 0
                }

    private fun needsNewTarget(): Boolean {
        val npc = target ?: return true
        if (attackStarted) return false
        if (!npc.isValid) return true
        if (npc.healthBarPercent <= 0.0) return true
        return false
    }

    private fun acquireTarget() {
        target = Query.npcs()
            .nameEquals("Giant rat")
            .filter { it.healthBarPercent > 0.0 && !it.isInteracting }
            .findClosest()
            .orElse(null)
    }

    private fun isAttackingTarget(): Boolean =
        MyPlayer.get()
            .map { player -> player.interactingCharacter.orElse(null) == target }
            .orElse(false)

    private fun ensureCombat() {
        val npc = target ?: return
        if (!npc.isValid || npc.healthBarPercent <= 0.0 || isAttackingTarget()) return

        if (npc.interact("Attack")) {
            attackStarted = Waiting.waitUntil(TutPreferences.longDelayMs()) {
                isAttackingTarget() || npc.isHealthBarVisible || npc.healthBarPercent < 100.0
            }
        }
    }

    private fun waitForDeath(): Boolean {
        if (!attackStarted) return false

        val npc = target ?: return true
        return !npc.isValid || npc.healthBarPercent <= 0.0
    }
}