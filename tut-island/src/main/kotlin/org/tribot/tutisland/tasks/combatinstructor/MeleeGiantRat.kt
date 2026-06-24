package org.tribot.tutisland.tasks.combatinstructor

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Npc
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class MeleeGiantRat: Task {
    override val displayName = "Meleeing Giant Rat"
    override val priority = 0

    private var target: Npc? = null
    private var attackStarted = false

    override fun canRun(): Boolean =
        GameState.getSetting(281) in 440..460

    override fun execute() {
        if (Inventory.getCount(ItemID.BRONZE_SWORD) > 0) {
            equip(ItemID.BRONZE_SWORD)
            return
        }

        if (Inventory.getCount(ItemID.WOODEN_SHIELD) > 0) {
            equip(ItemID.WOODEN_SHIELD)
            return
        }

        if (!Query.npcs().nameEquals("Giant rat").isReachable.isAny) {
            val gate = Query.gameObjects()
                .idEquals(9719)
                .actionEquals("Open")
                .isReachable
                .findClosest()
                .orElse(null)

            if (gate == null && !MyPlayer.isMoving()) {
                Walker.walkTo(Constants.combatInstructorArea.randomTile)
                return
            }

            if (gate.interact("Open")) {
                Waiting.waitUntil(TutPreferences.longDelayMs() * 3) {
                    Query.npcs().nameEquals("Giant rat").isReachable.isAny
                }
            }
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
            .isReachable
            .findBestInteractable()
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