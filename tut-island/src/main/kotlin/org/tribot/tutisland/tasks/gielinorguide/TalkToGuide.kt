package org.tribot.tutisland.tasks.gielinorguide

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class TalkToGuide: Task {
    override val displayName = "Talking To Gielinor Guide"
    override val priority = 0

    override fun canRun(): Boolean {
        val step = GameState.getSetting(281)

        return step == 2 || step == 7
    }

    override fun execute() {
        val npcName = "Gielinor Guide"

        if (!Query.npcs().nameEquals(npcName).isReachable.isAny) {
            Walker.walkTo(Constants.gielinorGuideArea.randomTile)
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