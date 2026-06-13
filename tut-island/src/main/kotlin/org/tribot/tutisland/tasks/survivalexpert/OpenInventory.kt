package org.tribot.tutisland.tasks.survivalexpert

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenInventory: Task {
    override val displayName = "Opening Inventory"
    override val priority = 0

    override fun canRun(): Boolean {
        return !GameTab.INVENTORY.isOpen &&
                GameState.getSetting(281) == 30
    }

    override fun execute() {
        if (!GameTab.INVENTORY.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.INVENTORY.isOpen }
        }
    }
}