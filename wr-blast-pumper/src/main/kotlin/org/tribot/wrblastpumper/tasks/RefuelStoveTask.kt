package org.tribot.wrblastpumper.tasks

import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.ObjectID
import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepHotReaction
import nullablelib.core.query.GroundItems
import nullablelib.core.query.TileObjects
import nullablelib.core.tabs.Inventory
import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrscript.utilities.hud.TaskLabelTracker
import org.tribot.wrscript.utilities.tasks.TaskContract

class RefuelStoveTask : TaskContract {
    override val name = "Refueling stove"

    override fun perform(): Boolean {
        if (ScriptArgsHelper.getOrDefault("refuel", "true") == "false") return true

        repeat(MAX_REFUEL_ATTEMPTS) {
            if (fullStove() != null) return true

            val stove = refillableStove() ?: return fullStove() != null
            if (!ensureCoke()) return false

            TaskLabelTracker.label = "Refueling stove"
            if (!ctx.interaction.interact(stove, REFUEL_ACTION)) return false

            val previousStoveId = stove.id
            val stoveChanged = Waiting.waitUntil(TribotRandom.uniform(750, 1_250)) {
                ctx.logger.debug("Waiting for stove to change")
                fullStove() != null || refillableStove()?.id != previousStoveId
            }

            if (!stoveChanged) {
                return false
            }
        }

        return fullStove() != null
    }

    private fun ensureCoke(): Boolean {
        TaskLabelTracker.label = "Collecting coke"
        if (Inventory.contains(ItemID.BLAST_FURNACE_COKE_SPADE)) return true
        if (!ensureSpade()) return false

        val coke = TileObjects.closestWithId(
            ObjectID.BLAST_FURNACE_COKE,
            maxDistance = INTERACTION_DISTANCE,
        ) ?: return false

        if (!ctx.interaction.interact(coke, COLLECT_ACTION)) return false

        sleepHotReaction()

        return Waiting.waitUntil(2_750, TribotRandom.uniform(350, 899)) {
            ctx.logger.debug("Waiting for inv to have a coke spade")
            Inventory.contains(ItemID.BLAST_FURNACE_COKE_SPADE)
        }
    }

    private fun ensureSpade(): Boolean {
        if (Inventory.contains(ItemID.SPADE) || Inventory.contains(ItemID.BLAST_FURNACE_COKE_SPADE)) return true
        TaskLabelTracker.label = "Grabbing spade"

        val spade = GroundItems.closestWithId(
            ItemID.SPADE,
            maxDistance = INTERACTION_DISTANCE,
        ) ?: return false

        if (Inventory.isFull()) {
            ctx.logger.error("Inventory is full, skipping spade grab")
            return false
        }

        if (!ctx.interaction.interact(spade, "Take")) return false

        return Waiting.waitUntil(5_000) {
            ctx.logger.debug("Waiting for inventory to register the spade")
            Inventory.contains(ItemID.SPADE)
        }
    }

    private fun refillableStove() = TileObjects.closestWithId(
        ObjectID.BLAST_FURNACE_STOVE_LOW,
        ObjectID.BLAST_FURNACE_STOVE_MEDIUM,
        maxDistance = STOVE_DISTANCE,
    )

    private fun fullStove() = TileObjects.closestWithId(
        ObjectID.BLAST_FURNACE_STOVE_FULL,
        maxDistance = STOVE_DISTANCE,
    )

    private companion object {
        const val STOVE_DISTANCE = 8
        const val INTERACTION_DISTANCE = 20
        const val MAX_REFUEL_ATTEMPTS = 5
        const val COLLECT_ACTION = "Collect"
        const val REFUEL_ACTION = "Refuel"
    }
}
