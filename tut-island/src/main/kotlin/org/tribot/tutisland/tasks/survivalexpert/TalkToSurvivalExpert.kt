package org.tribot.tutisland.tasks.survivalexpert

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class TalkToSurvivalExpert: Task {
    override val displayName = "Talking To Survival Expert"
    override val priority = 0

    override fun canRun(): Boolean {
        val step = GameState.getSetting(281)

        return step == 20 || step == 60
    }

    override fun execute() {
        val npcName = "Survival Expert"

        if (!Query.npcs().nameEquals(npcName).isReachable.isAny) {
            Walker.walkTo(Constants.survivalGuideArea.randomTile)
            return
        }

        if (!ChatScreen.isClickContinueOpen() && GameState.getVarbit(12393) == 0) {
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
            return
        }
    }
}