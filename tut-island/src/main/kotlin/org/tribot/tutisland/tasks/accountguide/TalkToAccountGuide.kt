package org.tribot.tutisland.tasks.accountguide

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class TalkToAccountGuide: Task {
    override val displayName = "Talking To Account Guide"
    override val priority = 0

    override fun canRun(): Boolean {
        val step = GameState.getSetting(281)

        return step == 530 || step == 532
    }

    override fun execute() {
        val npcName = "Account Guide"

        if (!Query.npcs().nameEquals(npcName).isReachable.isAny) {
            Walker.walkTo(Constants.accountGuideArea.randomTile)
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
                            GameState.getVarbit(12393) != 0
                        }
                    }
                }
            return
        }
    }
}