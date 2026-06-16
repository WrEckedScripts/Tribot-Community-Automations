package org.tribot.gemstoneminer.tasks

import org.tribot.gemstoneminer.util.GemStoneMinerPreferences
import org.tribot.gemstoneminer.util.data.Constants
import org.tribot.gemstoneminer.util.data.Vars
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.types.InventoryItem

class OpenGemStorage: Task {
    override val displayName: String
        get() = "Opening ${nextClosedGemStorage()?.name ?: "Gem Storage"}"

    override val priority = 5

    override fun canRun(): Boolean {
        return Vars.BankingDone &&
                !Bank.isOpen() &&
                Inventory.contains(*Constants.closedToOpenContainerIds.keys.toIntArray())
    }

    override fun execute() {
        Constants.closedToOpenContainerIds.forEach { (closedId, openId) ->
            if (!Inventory.contains(closedId) || Inventory.contains(openId)) {
                return@forEach
            }

            val item = nextClosedGemStorage(closedId) ?: return@forEach
            if (item.click("Open")) {
                Waiting.waitUntil(GemStoneMinerPreferences.mediumDelayMs()) {
                    Inventory.contains(openId) || !Inventory.contains(closedId)
                }
                Waiting.wait(GemStoneMinerPreferences.shortDelayMs())
            }
        }
    }

    private fun nextClosedGemStorage(id: Int? = null): InventoryItem? {
        val closedIds = if (id == null) {
            Constants.closedToOpenContainerIds.keys
        } else {
            setOf(id)
        }

        return Inventory.getAll().firstOrNull { item -> item.id in closedIds }
    }
}
