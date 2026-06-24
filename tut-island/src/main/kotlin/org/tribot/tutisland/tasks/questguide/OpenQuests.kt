package org.tribot.tutisland.tasks.questguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenQuests: Task {
    override val displayName = "Opening Quests"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 230
    }

    override fun execute() {
        if (!GameTab.QUESTS.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.QUESTS.isOpen }
        }
    }
}