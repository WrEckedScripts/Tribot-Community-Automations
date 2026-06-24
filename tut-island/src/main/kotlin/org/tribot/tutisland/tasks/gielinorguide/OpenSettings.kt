package org.tribot.tutisland.tasks.gielinorguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenSettings: Task {
    override val displayName = "Opening Settings"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 3
    }

    override fun execute() {
        if (!GameTab.OPTIONS.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.OPTIONS.isOpen }
        }
    }
}