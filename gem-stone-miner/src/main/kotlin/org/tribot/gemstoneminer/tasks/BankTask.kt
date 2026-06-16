package org.tribot.gemstoneminer.tasks

import org.tribot.gemstoneminer.util.GemStoneMinerPreferences
import org.tribot.gemstoneminer.util.Walker
import org.tribot.gemstoneminer.util.data.Constants
import org.tribot.gemstoneminer.util.data.MineLevel
import org.tribot.gemstoneminer.util.data.Pickaxe
import org.tribot.gemstoneminer.util.data.Vars
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.script.sdk.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.tasks.Amount
import org.tribot.script.sdk.tasks.EquipmentReq
import org.tribot.script.sdk.tasks.ItemReq
import org.tribot.script.sdk.tasks.BankTask as MiningBankTask

class BankTask: Task {
    override val displayName = "Banking"
    override val priority = 100

    override fun canRun(): Boolean {
        return !Vars.BankingDone
    }

    override fun execute() {
        if (!Bank.ensureOpen()) {
            val shiloBankerNearby = isShiloBankerNearby()

            // If in shilo walk to bank vs letting walker teleport. Check bank is nearby with NPC check
            if (Region.getCurrentRegionID() == MineLevel.UPPER.regionId) {
                if (!shiloBankerNearby) {
                    Walker.walkTo(Constants.shiloBank)
                    Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 2) { isShiloBankerNearby() }
                    return
                }

                openShiloBanker()
                Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs()) { Bank.isOpen() }
                return
            }

            Walker.walkToBank()
            Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 2) { Bank.isNearby() }
            return
        }

        emptyGemStorage()

        val bankTask = bankingTask()
        if (bankTask.execute().isEmpty) {
            Vars.BankingDone = true
            Bank.close()
        }
    }

    private fun emptyGemStorage() {
        Inventory.getAll()
            .filter { item -> item.id in Constants.allGemContainerIds }
            .forEach { storage ->
                if (storage.click("Empty")) {
                    Waiting.wait(GemStoneMinerPreferences.shortDelayMs())
                }
            }
    }

    private fun isShiloBankerNearby(): Boolean =
        Query.npcs()
            .nameEquals("Banker")
            .maxDistance(Constants.shiloBank, 8.0)
            .isAny

    private fun openShiloBanker() {
        val banker = Query.npcs()
            .nameEquals("Banker")
            .maxDistance(Constants.shiloBank, 8.0)
            .findClosestByPathDistance()
            .orElse(null)

        banker?.interact("Bank")
    }

    private fun bankingTask(): MiningBankTask =
        MiningBankTask.builder().apply {

            // Determine best owned pickaxe based on mining level. Equip if we have the attack req, else withdraw it.
            val pickaxe = bestPickaxe()
            if (Skill.ATTACK.actualLevel >= pickaxe.type.requiredAttackLevel) {
                addEquipmentItem { pickaxeEquipmentReq(pickaxe.id) }
            } else {
                addInvItem { ItemReq(pickaxe.id, Amount.of(1)) }
            }

            // Determine and equip best glory that has charge
            bestAvailableGloryId()?.let { gloryId ->
                addEquipmentItem { gloryEquipmentReq(gloryId) }
            }

            // Determine best gem storage and withdraw
            bestGemStorageIds().forEach { id ->
                addInvItem(id, Amount.of(1))
            }
        }.build()

    // Pick the highest-tier pickaxe the account can mine with
    private fun bestPickaxe(): PickaxeSelection {
        val usablePickaxes = Pickaxe.entries.filter { Skill.MINING.actualLevel >= it.requiredMiningLevel }
            .ifEmpty { listOf(Pickaxe.BRONZE) }

        usablePickaxes.forEach { pickaxe ->
            pickaxe.ids.firstOrNull { id -> hasAvailableItem(id) }?.let { id ->
                return PickaxeSelection(pickaxe, id)
            }
        }

        val fallback = usablePickaxes.first()
        return PickaxeSelection(fallback, fallback.ids.first())
    }

    // Gem sack stores all gems, so withdraw it alone. Other special gem storage take a gem bag too because they only stores special gems
    private fun bestGemStorageIds(): List<Int> {
        val sackId = bestAvailableStorageId(Constants.gemSackIds)
        if (sackId != null) {
            return listOf(sackId)
        }

        val storageIds = mutableListOf<Int>()
        val specialStorageId = bestSpecialGemStorageId()
        if (specialStorageId != null) {
            storageIds.add(specialStorageId)
            storageIds.add(requiredGemBagId())
            return storageIds
        }

        bestAvailableStorageId(Constants.gemBagIds)?.let(storageIds::add)
        return storageIds
    }

    // Finds the best owned special gem storage in tote -> satchel -> pouch priority order
    private fun bestSpecialGemStorageId(): Int? =
        Constants.specialGemStorageByPriority.firstNotNullOfOrNull(::bestAvailableStorageId)

    // Returns the first ID from a gem storage open/closed variant that is currently available
    private fun bestAvailableStorageId(storageIds: Set<Int>): Int? =
        storageIds.firstOrNull { id -> hasAvailableItem(id) }

    // Special gem storage requires a normal gem bag as a companion
    private fun requiredGemBagId(): Int =
        bestAvailableStorageId(Constants.gemBagIds) ?: Constants.gemBagIds.first()

    // Returns the best charged glory this is currently available
    private fun bestAvailableGloryId(): Int? =
        Constants.chargedGloryIds.firstOrNull { id -> hasAvailableItem(id) }

    // Equip the pickaxe in the weapon slot when the account has the attack level to wield it
    private fun pickaxeEquipmentReq(pickaxeId: Int): EquipmentReq =
        EquipmentReq.slot(Equipment.Slot.WEAPON).item(pickaxeId, Amount.of(1))

    // Equip the chosen charged glory in the necklace slot
    private fun gloryEquipmentReq(gloryId: Int): EquipmentReq =
        EquipmentReq.slot(Equipment.Slot.NECK).item(gloryId, Amount.of(1))

    // Return owned items from Inventory, equipment, and bank
    private fun hasAvailableItem(id: Int): Boolean =
        Inventory.getCount(id) > 0 || Equipment.getCount(id) > 0 || Bank.getCount(id) > 0

    private data class PickaxeSelection(
        val type: Pickaxe,
        val id: Int
    )
}
