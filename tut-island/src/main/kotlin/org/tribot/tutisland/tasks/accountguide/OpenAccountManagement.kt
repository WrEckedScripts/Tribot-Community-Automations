package org.tribot.tutisland.tasks.accountguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenAccountManagement: Task {
    override val displayName = "Opening Account Management"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 531
    }

    override fun execute() {
        if (!GameTab.ACCOUNT.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.ACCOUNT.isOpen }
        }
    }
}