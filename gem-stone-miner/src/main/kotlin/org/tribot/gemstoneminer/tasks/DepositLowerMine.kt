package org.tribot.gemstoneminer.tasks

import org.tribot.gemstoneminer.util.GemStoneMinerPreferences
import org.tribot.gemstoneminer.util.Walker
import org.tribot.gemstoneminer.util.data.Constants
import org.tribot.gemstoneminer.util.data.MineLevel
import org.tribot.gemstoneminer.util.data.Pickaxe
import org.tribot.gemstoneminer.util.data.Vars
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.script.sdk.*
import org.tribot.script.sdk.interfaces.Item
import org.tribot.script.sdk.query.Query

class DepositLowerMine: Task {
    override val displayName = "Depositing Gems"
    override val priority = 110

    override fun canRun(): Boolean {
        return !Vars.BankingDone &&
                Region.getCurrentRegionID() == MineLevel.LOWER.regionId &&
                (Inventory.contains(*Pickaxe.ids.toIntArray()) || Equipment.contains(*Pickaxe.ids.toIntArray()))
    }

    override fun execute() {
        if (Inventory.isEmpty()) {
            if (closeDepositBox()) {
                Vars.BankingDone = true
            }
            return
        }

        if (!Bank.isDepositBoxOpen()) {
            openDepositBox()
            return
        }

        emptyGemStorage()

        if (!hasDepositableItems()) {
            if (closeDepositBox()) {
                Vars.BankingDone = true
            }
            return
        }

        if (GameState.getVarbit(4430) != 2) {
            enableDepositAll()
            return
        }

        if (depositItems()) {
            Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs()) {
                !hasDepositableItems() || !Inventory.isFull()
            }
        }

        if (!hasDepositableItems() || !Inventory.isFull()) {
            if (closeDepositBox()) {
                Vars.BankingDone = true
            }
        }
    }

    private fun openDepositBox() {
        val depositBox = Query.gameObjects()
            .idEquals(10530)
            .findClosestByPathDistance()
            .orElse(null)

        if (depositBox == null) {
            Walker.walkTo(Constants.depositBoxTile, 1)
            return
        }

        if (depositBox.interact("Deposit")) {
            Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs()) { Bank.isDepositBoxOpen() }
            return
        }
    }

    // Widget 192.39 is the deposit-box "All" quantity button.
    private fun enableDepositAll() {
        val allButton = Widgets.get(192, 39).orElse(null) ?: return
        if (allButton.isVisible && allButton.click()) {
            Waiting.wait(GemStoneMinerPreferences.shortDelayMs())
        }
    }

    private fun emptyGemStorage() {
        getDepositBoxStorageWidgets().forEach { storage ->
            if (storage.click("Empty")) {
                Waiting.wait(GemStoneMinerPreferences.shortDelayMs())
            }
        }
    }

    // Re-query after each click because the deposit-box item widgets shift as slots empty
    private fun depositItems(): Boolean {
        var depositedAny = false

        repeat(28) {
            if (!Bank.isDepositBoxOpen() || !hasDepositableItems()) {
                return depositedAny
            }

            val item = nextDepositableWidget()
            if (item == null) {
                Log.warn("[GemStoneMiner] Deposit box is open, but no depositable item widgets were found.")
                return depositedAny
            }

            if (depositItem(item)) {
                depositedAny = true
                Waiting.wait(GemStoneMinerPreferences.shortDelayMs())
            } else {
                Waiting.wait(GemStoneMinerPreferences.shortDelayMs())
            }
        }

        if (Inventory.isFull() && hasDepositableItems()) {
            Log.warn("[GemStoneMiner] Deposit box did not clear all depositable items.")
        }

        return depositedAny
    }

    // Deposit-All and confirm at least one depositable item left the inventory.
    private fun depositItem(item: Item): Boolean {
        val countBefore = depositableItemCount()
        if (!item.click("Deposit-All")) {
            return false
        }

        Waiting.waitUntil(GemStoneMinerPreferences.mediumDelayMs()) {
            !Bank.isDepositBoxOpen() || depositableItemCount() < countBefore
        }
        return true
    }

    private fun nextDepositableWidget(): Item? =
        getDepositBoxDepositableWidgets().firstOrNull()

    private fun getDepositBoxDepositableWidgets(): List<Item> =
        getDepositBoxItems()
            .filter(::isDepositableItem)

    private fun getDepositBoxStorageWidgets(): List<Item> =
        getDepositBoxItems()
            .filter { item -> item.id in Constants.allGemContainerIds }

    private fun getDepositBoxItems(): List<Item> {
        val inventory = Widgets.get(192, 24).orElse(null) ?: return emptyList()
        val tableItems = inventory.toWidgetItemTable()
        val childItems = inventory.children.mapNotNull { child ->
            child.toWidgetItem().orElse(null)
        }

        return (tableItems + childItems)
            .distinctBy { item -> item.index to item.id }
    }

    private fun closeDepositBox(): Boolean {
        if (!Bank.isDepositBoxOpen()) {
            return true
        }

        Widgets.get(192, 1, 11)
            .ifPresent { widget -> widget.click("Close") }

        Waiting.waitUntil(GemStoneMinerPreferences.mediumDelayMs()) { !Bank.isDepositBoxOpen() }
        return !Bank.isDepositBoxOpen()
    }

    private fun hasDepositableItems(): Boolean =
        Inventory.getAll().any(::isDepositableItem)

    private fun isDepositableItem(item: Item): Boolean =
        item.id in Constants.uncutGemIds || item.name.containsClueOrScroll()

    private fun String.containsClueOrScroll(): Boolean =
        contains("clue", ignoreCase = true) || contains("scroll", ignoreCase = true)

    private fun depositableItemCount(): Int =
        Inventory.getAll()
            .filter(::isDepositableItem)
            .sumOf { item -> item.stack.coerceAtLeast(1) }
}