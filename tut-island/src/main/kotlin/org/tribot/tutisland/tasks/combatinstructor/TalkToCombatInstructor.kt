package org.tribot.tutisland.tasks.combatinstructor

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class TalkToCombatInstructor: Task {
    override val displayName = "Talking To Combat Instructor"
    override val priority = 0

    override fun canRun(): Boolean {
        val step = GameState.getSetting(281)

        return step == 370 || step == 410 || step == 470
    }

    override fun execute() {
        val npcName = "Combat Instructor"

        val equipmentWidget = Widgets.get(84, 3, 11).orElse(null)

        if (equipmentWidget != null && equipmentWidget.isVisible) {
            equipmentWidget.click()
            Waiting.waitUntil(TutPreferences.shortDelayMs()) { !equipmentWidget.isVisible }
            return
        }

        if (Query.npcs().nameEquals("Giant rat").isReachable.isAny) {
            Query.gameObjects().idEquals(9719).findBestInteractable().map { it.interact("Open") }
            Waiting.waitUntil(TutPreferences.longDelayMs() * 2) {
                !Query.npcs().nameEquals("Giant rat").isReachable.isAny
            }
            return
        }

        if (!Query.npcs().nameEquals(npcName).isReachable.isAny) {
            Walker.walkTo(Constants.combatInstructorArea.randomTile)
            return
        }

        Query.npcs()
            .nameEquals(npcName)
            .actionEquals("Talk-to")
            .isReachable
            .findBestInteractable()
            .ifPresent { npc ->
                if (npc.interact("Talk-to")) {
                    Waiting.waitUntil(TutPreferences.longDelayMs()) {
                        ChatScreen.isClickContinueOpen()
                    }
                }
            }
    }
}