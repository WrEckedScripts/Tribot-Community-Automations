package org.tribot.tutisland.tasks.brotherbrace

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenPrayer: Task {
    override val displayName = "Opening Prayer"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 560
    }

    override fun execute() {
        if (!GameTab.PRAYER.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.PRAYER.isOpen }
        }
    }
}